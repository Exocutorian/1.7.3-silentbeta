# BetaEngine

Модифицируемое ядро (framework) поверх Minecraft Beta 1.7.3. Цель — добавлять
контент через стабильный API, а не правками ванильного кода в десятках мест.

```
Game
 ├── Vanilla            net/minecraft/** (декомпилированный код игры)
 └── BetaEngine         betaengine/**
      ├── BetaEngine    точка входа, инициализация
      ├── Registry      регистрация блоков/предметов, авто-ID
      ├── Textures      фасад ресурс-лоадера (авто-ячейки атласов)
      ├── event/        EventBus + события игры
      ├── mod/          интерфейс Mod и загрузчик
      ├── client/       ТОЛЬКО клиент: сшивка атласов, ресурс-паки
      ├── textures/     PNG-текстуры движка/модов (только клиент)
      └── example/      референсный мод (медный блок и слиток)
```

Код движка (кроме `client/` и `textures/`) идентичен в
`minecraft/src/betaengine` и `minecraft_server/src/betaengine` — сторона
определяется в рантайме. При изменении правьте клиентскую копию и
синхронизируйте:

```sh
rm -rf minecraft_server/src/betaengine && mkdir -p minecraft_server/src/betaengine && \
  (cd minecraft/src/betaengine && find . -name '*.java' -not -path './client/*' | \
   while read f; do mkdir -p "../../../minecraft_server/src/betaengine/$(dirname "$f")"; \
   cp "$f" "../../../minecraft_server/src/betaengine/$f"; done)
```

## Registry

Ванильные конструкторы требуют числовой ID сразу, поэтому регистрация идёт
через фабрику: движок сам находит свободный ID и передаёт его в конструктор.

```java
Block copper = Registry.registerBlock("copper_block", new Registry.BlockFactory() {
    public Block create(int id) {
        return new BlockCopper(id);
    }
});

Item hammer = Registry.registerItem("hammer", new Registry.ItemFactory() {
    public Item create(int id) {
        return new ItemHammer(id);
    }
});
```

- Блоки получают ID начиная с 97 (ванилла занимает 1–96), предметы — первый
  свободный индекс от 256.
- Для каждого блока автоматически создаётся `ItemBlock`, чтобы блок существовал
  и как предмет.
- Строковое имя (`[a-z0-9_]+`) — стабильная идентичность контента;
  `Registry.getBlock("copper_block")`, `Registry.nameOf(block)`.

## События

`EventBus` — статическая шина. Диспатч идёт по иерархии классов: подписка на
`Event.class` ловит всё, на конкретный класс — только его. Упавший обработчик
логируется и не роняет игру.

```java
EventBus.subscribe(BlockBreakEvent.class, new EventHandler() {
    public void handle(Event event) {
        BlockBreakEvent e = (BlockBreakEvent)event;
        e.setCancelled(true); // блок не сломается
    }
});
```

Уже врезанные в ваниль хуки:

| Событие | Где постится | Отменяемое |
| --- | --- | --- |
| `EngineInitEvent` | после init всех модов | нет |
| `BlockBreakEvent` | клиент `PlayerControllerSP.sendBlockRemoved`, сервер `ItemInWorldManager.func_325_c` | да |
| `PlayerTickEvent` | `EntityPlayer.onUpdate` (обе стороны) | нет |

## Resource Loader

Текстуры больше не требуют ручного редактирования атласов. Кладёте PNG
16×16 и просите ячейку:

```java
// в конструкторе блока
this.blockIndexInTexture = Textures.block("copper_block");
// в конструкторе предмета
this.setIconIndex(Textures.item("copper_ingot"));
```

Движок сам находит свободные ячейки в `terrain.png` / `gui/items.png`
(в b1.7.3 их 86 и 107) и при загрузке атласа дорисовывает туда ваши PNG.
Свободные ячейки вычисляются в рантайме перебором всех блоков
(стороны × метадата) и предметов (damage), а не захардкоженным списком;
отдельно зарезервированы ячейки, зависящие от мира: двойные сундуки,
заснеженная трава, 2×2-анимации потоков воды/лавы, ряд трещин 240–255.

Файл текстуры ищется по пути `betaengine/textures/blocks/<имя>.png`
(или `items/`) в таком порядке:

1. **Папка** `resources/betaengine/textures/...` в директории игры —
   быстрый способ подменить текстуру без пересборки;
2. **Выбранный текстур-пак** — тот же путь внутри zip;
3. **Classpath** — PNG, лежащие рядом с кодом
   (`minecraft/src/betaengine/textures/...`, RetroMCP копирует их в сборку).

Если файл не найден нигде — в ячейку рисуется пурпурно-чёрная «missing
texture», игра не падает.

Врезка одна: `TexturePackBase/TexturePackCustom.getResourceAsStream` —
через неё проходят и первая загрузка, и `refreshTextures`. На сервере
`Textures.*` возвращает 0 (индексы текстур не сохраняются и не ходят по
сети, так что это безопасно).

## Ресурс-паки

Используются **ванильные** текстур-паки Beta (меню Mods & Texture Packs,
папка `texturepacks/`, zip-файлы) — движок делает их совместимыми с
модовым контентом:

- пак может переопределить текстуру мода, положив PNG по пути
  `betaengine/textures/blocks/copper_block.png` внутри zip;
- HD-паки (32×, 64×...) работают: ячейка масштабируется под размер атласа;
- смена пака в меню на лету пересшивает атласы (`refreshTextures`
  проходит через ту же врезку).

## Моды

```java
public class MyMod implements Mod {
    public String id() { return "mymod"; }
    public void init() { /* Registry.*, EventBus.subscribe */ }
}
```

Пока моды компилируются вместе с игрой и регистрируются в
`BetaEngine.init()` (`Mods.register(new MyMod())`). Загрузка внешних папок
`mods/` — в планах, интерфейс останется тем же.

## Точки входа в ванильном коде

Весь движок цепляется к игре через минимум правок:

- `net/minecraft/client/Minecraft.startGame` → `BetaEngine.init()` (клиент)
- `net/minecraft/server/MinecraftServer.startServer` → `BetaEngine.init()` (сервер)
- `PlayerControllerSP.sendBlockRemoved`, `ItemInWorldManager.func_325_c` → `BlockBreakEvent`
- `EntityPlayer.onUpdate` (обе стороны) → `PlayerTickEvent`
- `Minecraft.startGame` → `ClientResources.install(this)` (до `init()`)
- `TexturePackBase/TexturePackCustom.getResourceAsStream` → сшивка атласов

## Дорожная карта

1. **Больше событий** — PlayerJoinEvent, ChunkLoadEvent, BlockPlaceEvent,
   EntitySpawnEvent, крафт/печка.
2. **JSON-описания блоков/предметов** — декларативный контент без Java-класса.
3. **Персистентные ID-карты** — миры хранят числовые ID (и блоков, и ячеек
   атласа); нужен level.dat-мэппинг имя→ID на мир, чтобы порядок регистрации
   модов не ломал сохранения.
4. **Внешние моды** — загрузка `mods/*/classes.jar` + `mod.json` через
   URLClassLoader, авторегистрация по `@Register`-аннотации.
5. **Item/Entity/GUI/WorldGen/Config API** — по мере надобности поверх
   Registry и EventBus.
6. **Языковые файлы модов** — `copperBlock.name` и т.п. через свои .lang.
