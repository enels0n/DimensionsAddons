# DimensionsConditionalCommandsOnUseAddon

Runs commands when a Dimensions portal is used, but only if configurable conditions match.

Example portal config:

```yml
Addon:
  ConditionalCommandsOnUse:
    actions:
      player_enter:
        conditions:
          type: all
          checks:
            - type: "string equals"
              input: "%entity_type%"
              output: "PLAYER"
            - type: "string equals"
              input: "%portal_id%"
              output: "example_portal"
        commands:
          - "[console] say %player_name% used %portal_id%"
      zombie_enter:
        conditions:
          type: all
          checks:
            - type: "string equals"
              input: "%entity_type%"
              output: "ZOMBIE"
        commands:
          - "[console] say Zombie used portal %portal_id%"
```

Built-in replacements:

- `%entity_type%`
- `%entity_name%`
- `%entity_uuid%`
- `%is_player%`
- `%player_name%`
- `%player_uuid%`
- `%portal_id%`
- `%portal_display_name%`
- `%portal_world%`
- `%destination_portal_id%`
- `%destination_portal_display_name%`
- `%destination_world%`

If PlaceholderAPI is installed and the entity is a player, player placeholders also work in `input`, `output`, and `commands`.
