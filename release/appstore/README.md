# Ficha de App Store — Shareat (es-ES)

Textos y recursos gráficos listos para copiar en App Store Connect. El idioma
principal de la ficha es **español (España)**, que es también el idioma de
lanzamiento del producto.

- Bundle ID: `org.shareat.app.shareat`
- Categoría principal: **Comida y bebida**
- Categoría secundaria: **Estilo de vida**
- Clasificación por edad sugerida: **12+** (contenido generado por usuarios)
- URL de soporte: `https://shareatofficial.github.io/app/`
- URL de eliminación de cuenta: `https://shareatofficial.github.io/app/removeAccount/`
- Términos: `https://shareatofficial.github.io/app/terms-and-conditions.html`

---

## 1. Nombre de la app

*(máximo 30 caracteres)*

```
Shareat: valora cada plato
```

26 caracteres.

---

## 2. Subtítulo

*(máximo 30 caracteres — aparece bajo el nombre en resultados y en la ficha)*

```
Reseñas y alérgenos por plato
```

29 caracteres.

---

## 3. Texto promocional

*(máximo 170 caracteres — se puede cambiar sin enviar una versión nueva)*

```
La nota media de un restaurante no te dice qué pedir. En Shareat cada plato tiene su propia nota, su precio y los alérgenos que declara la casa.
```

144 caracteres.

---

## 4. Descripción

*(máximo 4000 caracteres)*

```
Shareat es la app donde no solo valoras el restaurante: valoras cada plato.

Antes de reservar o de sentarte, mira la carta entera, el precio real de cada plato y lo que opina la gente que ya lo ha probado. Y si tienes una alergia o una intolerancia, filtra la carta y quédate solo con lo que puedes comer.

PARA QUIEN SALE A COMER

• Descubre restaurantes cerca de ti, con su nota media y su horario, y entra a ver la carta sin registrarte.
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

Shareat está disponible en iPhone, Android y web.
```

---

## 5. Palabras clave

*(máximo 100 caracteres, separadas por comas y sin espacios tras la coma)*

```
restaurantes,carta,menu,alergenos,reseñas,valorar,platos,gastronomia,comer,tapas,celiaco,intolerancia
```

101 caracteres — si App Store Connect lo rechaza por un carácter, quita
`intolerancia` y quedan 88.

No repitas en las palabras clave lo que ya está en el nombre ni en el
subtítulo: App Store indexa los tres campos juntos.

---

## 6. Novedades de esta versión

*(máximo 4000 caracteres)*

```
Primera versión de Shareat.

• Descubre restaurantes con su nota media, horario y carta completa.
• Filtra la carta por los alérgenos que declara cada restaurante.
• Valora y comenta plato a plato, del 1 al 5.
• Los restaurantes dan de alta su perfil, montan su carta con precios y alérgenos, y deciden cuándo publicarla.
• Consulta tu historial de reseñas en la pestaña Actividad.
```

---

## 7. Recursos gráficos

| Recurso | Archivo | Formato exigido por App Store |
| --- | --- | --- |
| Icono de la app | `graphics/icon-1024.png` | 1024 × 1024, PNG sin canal alfa |
| Capturas de iPhone 6,9" | `screenshots/es/*.png` | 8 imágenes, 1320 × 2868, PNG |

App Store no usa gráfico de funciones (eso es exclusivo de Google Play). Las
capturas de 6,9" se reescalan automáticamente para los tamaños de iPhone más
pequeños, así que no hace falta un segundo juego.

Las capturas están tomadas en el simulador **iPhone 17 Pro Max (iOS 26.5)** con
la app real en modo depuración, apuntando al Supabase de desarrollo. La barra de
estado se fija con `xcrun simctl status_bar override` a la hora canónica de
Apple (09:41), cobertura y batería llenas.

---

## 8. Capturas y frases

Las cuatro primeras son el recorrido de cliente y las cuatro últimas el de
restaurante. Cada imagen lleva impresa su frase y una etiqueta de color
(naranja para **Clientes**, verde azulado para **Restaurantes**).

### Clientes

| # | Archivo | Titular | Apoyo |
| --- | --- | --- | --- |
| 1 | `01-descubre.png` | Descubre dónde comer hoy | Restaurantes cerca de ti, con su nota media y si están abiertos ahora. |
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

## 9. Privacidad de la app (App Privacy)

Declaraciones que corresponden al alcance actual de la app. Ningún dato se usa
para seguimiento publicitario, así que **App Tracking Transparency no aplica** y
no hay que incluir `NSUserTrackingUsageDescription`.

| Categoría | Datos | Vinculados a la identidad | Uso |
| --- | --- | --- | --- |
| Información de contacto | Correo electrónico; teléfono opcional en el perfil | Sí | Funcionalidad de la app |
| Identificadores | ID de usuario | Sí | Funcionalidad de la app |
| Contenido del usuario | Reseñas, puntuaciones, fotos de platos | Sí | Funcionalidad de la app |
| Datos de uso | Ninguno | — | — |
| Ubicación | Ninguna. La app no pide permiso de ubicación | — | — |

- Cifrado en tránsito: sí.
- No se venden datos a terceros y la app no muestra publicidad.
- No hay notificaciones push ni acceso a contactos.
- Eliminación de cuenta: se solicita desde
  `https://shareatofficial.github.io/app/removeAccount/`. App Store exige que
  esta URL esté declarada en el campo *Account Deletion* de App Store Connect.

### Cifrado (App Store Connect)

La app solo usa HTTPS estándar del sistema. Corresponde responder que usa
cifrado **exento**, lo que en el `Info.plist` equivale a
`ITSAppUsesNonExemptEncryption = NO`.

---

## 10. Contenido generado por usuarios

Los comentarios nuevos o editados esperan moderación antes de aparecer al
público; las valoraciones sin comentario se publican de inmediato. Cada
comentario visible permite denunciarlo y bloquear a su autor. El bloqueo
oculta las reseñas de ese autor para la cuenta que lo activó. Las denuncias
se guardan en una cola privada que debe tramitarse con rapidez. Los
restaurantes no pueden borrar ni ocultar reseñas, ni puntuar platos o locales:
solo las cuentas de cliente pueden valorar. El contacto público es
`shareat.app.official@gmail.com`.

La migración de moderación tiene que estar aplicada en producción antes de
instalar la nueva compilación. Hay que revisar la cola de denuncias y los
comentarios pendientes a diario; los pasos están en
`release/appstore/REVIEW_RESUBMISSION.md`.

---

## 11. Datos de demostración para App Review

El revisor necesita una cuenta de restaurante y una de cliente. Para probar
denuncia y bloqueo debe haber una reseña visible de **otro** cliente: prepara
una segunda cuenta de cliente o un contenido de muestra autorizado. Las
credenciales se anotan en *App Review Information → Sign-in required* y
*Notes*, con una nota del tipo:

> La app se puede explorar sin cuenta. Para probar la gestión de restaurante,
> usa la cuenta de demostración adjunta: incluye un restaurante publicado con
> carta y alérgenos.

---

## 12. Fuera del alcance de esta versión

Conviene no prometer en la ficha nada de lo siguiente, que está explícitamente
fuera del MVP: feed social y seguidores, listas públicas o colaborativas,
búsqueda avanzada en mapa, varias cartas por restaurante, cartas por QR,
notificaciones push, verificación de restaurantes, verificación de alérgenos y
analíticas de pago.

**Shareat Unlimited se reserva para una actualización posterior.** Su propósito
previsto es mostrar a los restaurantes qué productos reciben reseñas y
estadísticas de la opinión de los clientes. No aparece ningún flujo de compra
en la compilación inicial.

---

## 13. Cómo regenerar los recursos

1. Compilar e instalar la app en el simulador:
   `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max' build`
   y después `xcrun simctl install booted <ruta>.app`.
2. Fijar la barra de estado:
   `xcrun simctl status_bar booted override --time "09:41" --cellularMode active --cellularBars 4 --wifiMode active --wifiBars 3 --batteryState discharging --batteryLevel 100`.
   Para restaurarla: `xcrun simctl status_bar booted clear`.
3. Lanzar la app desde el SpringBoard (no desde Ajustes) para que no aparezca la
   miga de pan «◀ Ajustes» en la barra de estado.
4. Capturar cada pantalla con
   `xcrun simctl io booted screenshot --type=png raw_c1.png`, siguiendo los
   nombres `raw_c1..raw_c4` (cliente) y `raw_r1..raw_r4` (restaurante).
5. Componer las imágenes finales:
   `python3 release/tools/compose_screenshots.py appstore --raw <directorio>`.
6. El icono de 1024 sale del asset de la propia app,
   `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-light.png`,
   aplanado sobre blanco porque App Store no admite canal alfa.

Las tipografías de los rótulos son las de la propia marca: **Fraunces** para
los titulares e **Inter** para el texto de apoyo, ambas en
`shared/designsystem/src/commonMain/composeResources/font/`. Los colores son
los del tema: naranja `#FF4F00`, verde azulado `#006874` y crema `#FFF8F6`.
