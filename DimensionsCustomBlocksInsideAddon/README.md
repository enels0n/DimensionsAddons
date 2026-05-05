# DimensionsCustomBlocksInsideAddon

Addon for `Dimensions` that replaces the inside of a portal with blocks from `SopCustomBlocks`.

## What it does

- sets the `hidePortalInside` tag on `CustomPortalIgniteEvent` so `Dimensions` does not render its default portal inside;
- fills the portal interior with a custom block from `SopCustomBlocks`;
- removes those custom blocks again on `CustomPortalBreakEvent`.

## Portal config

Add this to the target portal config:

```yml
Addon:
  SopCustomBlocksInside:
    BlockId: portal_inside
```

- `BlockId` is the custom block id from `SopCustomBlocks`.

## SopCustomBlocks setup

The block used as the portal interior will usually need `replacement-block: AIR` so it can exist in open air.
An example is available in `examples/sopcustomblocks-blocks.yml`.

## Build

Run:

```bash
mvn package
```

## Install

Put the built addon jar where your `Dimensions` addons are loaded from.
