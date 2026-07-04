# BetaEngine

Модифицируемое ядро (framework) поверх Minecraft Beta 1.7.3. Цель — добавлять
контент через стабильный API, а не правками ванильного кода в десятках мест.

```
Game
 ├── Vanilla            net/minecraft/** (декомпилированный код игры)
 └── BetaEngine         betaengine/**
      ├── BetaEngine    точка входа, инициализация
      ├── Registry      регистрация блоков/предметов, авто-ID
      ├── event/        EventBus + события игры
      ├── mod/          интерфейс Mod и загрузчик
      └── example/      референсный мод (медный блок)
```

Код движка идентичен в `minecraft/src/betaengine` и
`minecraft_server/src/betaengine` (сторона определяется в рантайме).
При изменении движка правьте клиентскую копию и синхронизируйте:

```sh
rm -rf minecraft_server/src/betaengine && cp -r minecraft/src/betaengine minecraft_server/src/
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

## Дорожная карта

1. **Resource Loader** — сборка `resources/textures/**` в атласы
   (terrain.png / items.png) на старте, чтобы не редактировать атласы руками.
2. **Больше событий** — PlayerJoinEvent, ChunkLoadEvent, BlockPlaceEvent,
   EntitySpawnEvent, крафт/печка.
3. **JSON-описания блоков/предметов** — декларативный контент без Java-класса.
4. **Персистентные ID-карты** — миры хранят числовые ID; нужен level.dat-мэппинг
   имя→ID на мир, чтобы порядок регистрации модов не ломал сохранения.
5. **Внешние моды** — загрузка `mods/*/classes.jar` + `mod.json` через
   URLClassLoader, авторегистрация по `@Register`-аннотации.
6. **Item/Entity/GUI/WorldGen/Config API** — по мере надобности поверх
   Registry и EventBus.
7. **Ресурс-паки** — переключаемые наборы текстур в меню.
