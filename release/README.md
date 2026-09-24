# Recursos de publicación

Textos e imágenes de las fichas de tienda de Shareat. Todo está en español
(España), que es el idioma de lanzamiento del producto.

| Tienda | Ficha | Capturas | Gráficos |
| --- | --- | --- | --- |
| Google Play | [`playstore/README.md`](playstore/README.md) | `playstore/screenshots/es/` (1080 × 1920) | `playstore/graphics/` (icono 512 y gráfico de funciones) |
| App Store | [`appstore/README.md`](appstore/README.md) | `appstore/screenshots/es/` (1320 × 2868) | `appstore/graphics/` (icono 1024) |

Las capturas salen de la app real —emulador Pixel 9 (API 36) para Android y
simulador iPhone 17 Pro Max (iOS 26.5) para iOS— apuntando al Supabase de
desarrollo. Las ocho frases son las mismas en las dos tiendas: cuatro del
recorrido de cliente y cuatro del de restaurante.

Para recomponer las imágenes finales a partir de las capturas en bruto:

```bash
python3 release/tools/compose_screenshots.py play --raw <directorio>
python3 release/tools/compose_screenshots.py appstore --raw <directorio>
```

Las frases viven en `release/tools/compose_screenshots.py`, así que cambiarlas
ahí actualiza las dos tiendas a la vez. Cada ficha detalla en su apartado
«Cómo regenerar los recursos» el resto del procedimiento.

## Pendiente antes de publicar

Ambas tiendas exigen un mecanismo de denuncia del contenido generado por
usuarios dentro de la app, que todavía no existe. App Store además pide poder
bloquear a otros usuarios. Está detallado en cada ficha.
