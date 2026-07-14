# vaultwarden stack module

- Module id: `vaultwarden`
- Module repo: `vaultwarden-stack-module`
- Source repo: none declared
- Lifecycle: `active`

## Owned overlays
- `stack.runtime.yaml`
- `stack.config/vaultwarden`
- `stack.containers/vaultwarden-maintenance`

## Dependencies
- `stack-foundation`

## Validation

```sh
./tests/validate.sh
```

## Lifecycle

`active` modules are expected to keep `stack.module.json`, owned overlays, and `tests/validate.sh` in sync.
