# Ficha de Google Play — Shareat (es-ES)

Textos y recursos gráficos listos para copiar en Google Play Console. El idioma
principal de la ficha es **español (España)**, que es también el idioma de
lanzamiento del producto.

- Paquete: `org.shareat.app`
- Categoría: **Comida y bebida**
- Etiquetas sugeridas: restaurantes, reseñas, cartas, alérgenos, gastronomía
- Clasificación de contenido: apta para todos los públicos (contenido generado
  por usuarios con moderación reactiva)
- URL de asistencia: `https://shareatofficial.github.io/app/`
- URL de eliminación de cuenta: `https://shareatofficial.github.io/app/removeAccount/`
- Términos: `https://shareatofficial.github.io/app/terms-and-conditions.html`
- Correo de contacto y moderación: `shareat.app.official@gmail.com`

---

## 1. Nombre de la aplicación

*(máximo 30 caracteres)*

```
Shareat: valora cada plato
```

26 caracteres.

---

## 2. Descripción breve

*(máximo 80 caracteres — es el texto que aparece bajo el icono)*

```
Descubre restaurantes y valora plato a plato, con alérgenos siempre al día.
```

75 caracteres.

---

## 3. Descripción completa

*(máximo 4000 caracteres)*

```
Shareat es la app donde no solo valoras el restaurante: valoras cada plato.

Antes de reservar o de sentarte, mira la carta entera, el precio real de cada plato y lo que opina la gente que ya lo ha probado. Y si tienes una alergia o una intolerancia, filtra la carta y quédate solo con lo que puedes comer.

PARA QUIEN SALE A COMER

• Explora restaurantes disponibles en Shareat, con su nota media y su horario, y entra a ver la carta sin registrarte.
• Consulta la carta completa: cada plato con su nombre, su descripción, su precio y los alérgenos que declara el restaurante.
• Filtra por alérgenos. El filtro solo te ofrece los alérgenos que declara esa carta concreta, y nunca esconde un plato por falta de información.
• Valora plato a plato del 1 al 5 y escribe un comentario si te apetece. Cada plato tiene su propia nota, no solo el local.
• Consulta tu historial de reseñas en la pestaña Actividad. Si vuelves a valorar un plato, tu reseña se actualiza en lugar de duplicarse.

PARA RESTAURANTES

• Date de alta en un paso: nombre, descripción, contacto y dirección. Tu restaurante nace como borrador, así que nadie lo ve hasta que tú quieras.
• Monta tu carta plato a plato, con precio, descripción, foto opcional y los 14 alérgenos de la normativa europea.
• Activa o desactiva platos sueltos sin tener que borrarlos.
• Publica cuando lo tengas listo. Un interruptor decide si tu restaurante y su carta son visibles para los clientes.
• Usa la vista de cliente para comprobar cómo se ve tu carta antes de publicarla.
• Edita el perfil, el horario semanal y los datos de contacto cuando cambien: lo que gestionas es exactamente lo que ven tus clientes.

POR QUÉ SHAREAT

Las notas medias de un restaurante esconden demasiado. Un sitio con un 4,2 puede tener un plato redondo y otro que no repetirías. Shareat reparte la nota donde de verdad importa, en el plato, para que sepas qué pedir y no solo dónde entrar.

SOBRE LOS ALÉRGENOS

La información de alérgenos la publica cada restaurante y Shareat la muestra tal cual, sin interpretarla. Que un plato no declare un alérgeno no significa que no lo contenga: confirma siempre con el restaurante si tienes una alergia.

NAVEGA SIN CUENTA

Explorar restaurantes, cartas y reseñas públicas no requiere cuenta. Solo necesitas registrarte para escribir reseñas o para gestionar un restaurante.

IDIOMA

Shareat está en español y en inglés, y puedes cambiar el idioma desde Ajustes sin salir de la app.

Shareat está disponible en Android, iOS y web.
```

2494 caracteres.

---

## 4. Novedades de la versión

*(máximo 500 caracteres)*

```
Primera versión de Shareat.

• Descubre restaurantes con su nota media, horario y carta completa.
• Filtra la carta por los alérgenos que declara cada restaurante.
• Valora y comenta plato a plato, del 1 al 5.
• Los restaurantes dan de alta su perfil, montan su carta con precios y alérgenos, y deciden cuándo publicarla.
• Consulta tu historial de reseñas en la pestaña Actividad.
```

---

## 5. Recursos gráficos

| Recurso | Archivo | Formato exigido por Play |
| --- | --- | --- |
| Icono de alta resolución | `graphics/icon-512.png` | 512 × 512, PNG 32 bits |
| Gráfico de funciones | `graphics/feature-graphic.png` | 1024 × 500, PNG o JPEG sin transparencia |
| Capturas de teléfono | `screenshots/es/*.png` | 8 imágenes, 1080 × 1920 (9:16), PNG |

El gráfico de funciones se recorta por los bordes en algunas superficies de
Play, por lo que el texto y el icono están dentro de un margen de seguridad
amplio.

Las capturas están tomadas en un emulador **Pixel 9 (API 36)** con la app real
en modo depuración, apuntando al Supabase de desarrollo. Los iconos de la barra
de estado se ocultan durante la captura para que las imágenes no muestren la
hora ni la batería del equipo.

---

## 6. Capturas y frases

Las cuatro primeras son el recorrido de cliente y las cuatro últimas el de
restaurante. Cada imagen lleva impresa su frase y una etiqueta de color
(naranja para **Clientes**, verde azulado para **Restaurantes**).

### Clientes

| # | Archivo | Titular | Apoyo |
| --- | --- | --- | --- |
| 1 | `01-descubre.png` | Descubre dónde comer hoy | Restaurantes publicados en Shareat, con su nota y si están abiertos ahora. |
| 2 | `02-carta.png` | La carta entera, antes de sentarte | Cada plato con su precio, su descripción y los alérgenos que declara el local. |
| 3 | `03-alergenos.png` | Filtra por alérgenos | Solo aparecen los alérgenos que declara esa carta. Nada se da por supuesto. |
| 4 | `04-valora-platos.png` | Valora plato a plato | No solo el restaurante: cada plato tiene su nota y sus reseñas. |

### Restaurantes

| # | Archivo | Titular | Apoyo |
| --- | --- | --- | --- |
| 5 | `05-alta-restaurante.png` | Da de alta tu restaurante | Nombre, contacto y dirección en un paso. Empieza como borrador, sin prisa. |
| 6 | `06-anadir-plato.png` | Añade cada plato con sus alérgenos | Precio, descripción y los 14 alérgenos de la normativa europea. |
| 7 | `07-gestiona-carta.png` | Tu carta, siempre al día | Añade platos, ordena categorías y cambia precios desde el móvil. |
| 8 | `08-vista-cliente.png` | Compruébalo como lo ve tu cliente | La vista de cliente te enseña tu carta tal y como llega a la gente. |

---

## 7. Seguridad de los datos

Declaraciones que corresponden al alcance actual de la app:

- **Datos recogidos:** dirección de correo y contraseña para la cuenta; nombre
  público y, opcionalmente, nombre completo y teléfono en el perfil de cliente;
  datos públicos del restaurante (nombre, descripción, contacto y dirección) en
  las cuentas de restaurante; reseñas y puntuaciones.
- **Finalidad:** funcionamiento de la app y gestión de la cuenta.
- **Cifrado en tránsito:** sí.
- **Eliminación de datos:** la app todavía no ofrece borrado de cuenta desde la
  propia aplicación, pero la vía de eliminación que exige Play ya está
  publicada: `https://shareatofficial.github.io/app/removeAccount/`. Hay que
  declararla en Play Console en *Seguridad de los datos → Eliminación de
  cuenta*.
- **Datos compartidos con terceros:** no se venden datos a terceros.
- **Anuncios:** la app no muestra publicidad.

La app no incluye notificaciones push, no usa ubicación en segundo plano y no
accede a contactos.

---

## 8. Contenido generado por usuarios

Shareat publica las reseñas sin moderación previa. Los restaurantes no pueden
borrar ni ocultar las reseñas que reciben, ni pueden puntuar restaurantes o
platos: solo las cuentas de cliente pueden valorar, y la regla se aplica en el
backend, no solo en la interfaz.

> **Pendiente antes de publicar.** Play exige un canal para denunciar
> contenido generado por usuarios. La app todavía no tiene un botón de denuncia
> en la reseña (la moderación existe como política, no como función). Para el
> primer envío se declara `shareat.app.official@gmail.com` como canal de
> denuncia; la denuncia dentro de la app sigue pendiente de implementar.

---

## 9. Fuera del alcance de esta versión

Conviene no prometer en la ficha nada de lo siguiente, que está explícitamente
fuera del MVP: feed social y seguidores, listas públicas o colaborativas,
búsqueda avanzada en mapa, varias cartas por restaurante, cartas por QR,
notificaciones push, verificación de restaurantes, verificación de alérgenos y
analíticas de pago.

---

## 10. Cómo regenerar los recursos

Las capturas se toman de la app real en un emulador. Resumen del
procedimiento seguido:

1. Arrancar el emulador `Pixel_9_API_36_2` e instalar la app:
   `./gradlew :androidApp:installDebug`.
2. Ocultar los iconos de la barra de estado para que no salgan en las capturas:
   `adb shell settings put secure icon_blacklist "clock,wifi,mobile,battery,..."`.
   Para restaurar el emulador: `adb shell settings delete secure icon_blacklist`.
3. Navegar cada pantalla y capturarla con
   `adb exec-out screencap -p > pantalla.png`. Desactiva el teclado
   (`adb shell ime disable <id>`) antes de teclear: el autocorrector de Gboard
   altera los textos en español y mueve las coordenadas de los toques.
4. Componer las imágenes finales con las frases y el marco de dispositivo:
   `python3 release/tools/compose_screenshots.py play --raw <directorio>`.
   Las capturas se nombran `raw_c1..raw_c4` (cliente) y `raw_r1..raw_r4`
   (restaurante); el script recorta los 142 px de barra de estado de Android.
5. Generar el gráfico de funciones y el icono a partir de los recursos de marca
   de `:shared:designsystem` y del icono de `iosApp`.

La ficha equivalente de App Store, con las mismas frases sobre capturas de
iPhone, está en `release/appstore/README.md`.

Las tipografías de los rótulos son las de la propia marca: **Fraunces** para
los titulares e **Inter** para el texto de apoyo, ambas en
`shared/designsystem/src/commonMain/composeResources/font/`. Los colores son
los del tema: naranja `#FF4F00`, verde azulado `#006874` y crema `#FFF8F6`.
