# DimensionsCustomBlocksInsideAddon

Аддон для `Dimensions`, который подменяет внутренность портала на блоки из `ACustomBlocks`.

## Что делает

- на `CustomPortalIgniteEvent` ставит тег `hidePortalInside`, чтобы `Dimensions` не рисовал свою стандартную внутренность;
- заполняет внутренний объём портала кастомным блоком из `ACustomBlocks`;
- на `CustomPortalBreakEvent` удаляет эти кастомные блоки обратно.

## Настройка портала

В конфиге нужного портала добавь:

```yml
Addon:
  CustomBlocksInside:
    Enabled: true
    BlockId: portal_inside
    RemoveDropsOnBreak: true
```

- `Enabled` — включает аддон для конкретного портала.
- `BlockId` — id блока из `ACustomBlocks/blocks.yml`.
- `RemoveDropsOnBreak` — если `true`, при разрушении портала аддон удаляет visual/custom block без дропа; если `false`, использует `breakBlock(...)`, что может дать дроп.

## Настройка ACustomBlocks

У блока, который будет выступать внутренностью портала, обычно нужен `replacement-block: AIR`, чтобы он мог существовать в воздухе.
Пример лежит в `examples/acustomblocks-blocks.yml`.

## Сборка

1. Положи `Dimensions.jar` в `libs/Dimensions.jar`
2. Положи `ACustomBlocks.jar` в `libs/ACustomBlocks.jar`
3. При необходимости выровняй версии API/Java под свой сервер
4. Выполни:

```bash
mvn package
```

## Установка

Собранный jar аддона положи туда же, где грузятся аддоны `Dimensions`.
