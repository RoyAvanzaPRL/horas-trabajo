# AGENTS.md

Este archivo es la memoria de las sesiones `/grill-me` de este repositorio: aquí se
registran las decisiones de diseño ya cerradas, para que cualquier sesión futura
(humana o de agente) tenga el contexto sin tener que re-preguntarlo.

## Alcance del repositorio

Este repositorio es exclusivamente la app de **registro de horas de trabajo
personal**. La idea inicial de una app de finanzas personales para esta carpeta
quedó descartada por el usuario — no se va a desarrollar.

## Estado de la implementación

Scaffold completo implementado (2026-07-24): proyecto Gradle (Kotlin + Jetpack
Compose + Room + kotlinx.serialization), capa de datos, dominio, repositorios,
UI (Trabajos / plantilla mensual / formulario de entrada / resumen), y export
JSON/PDF/JPEG — todo según las decisiones de abajo. **Verificado compilando de
verdad**: se instaló un JDK 17 y un Android SDK (API 34) temporales en este
entorno y se ejecutó `gradle :app:assembleDebug` con éxito (BUILD SUCCESSFUL,
APK generado), no es solo código "que parece correcto a ojo". Se corrigieron
dos errores reales de compilación encontrados así (conversión Int/Long en
`Converters.kt`, Float/Int en `JpegExporter.kt`) y warnings de opt-in.

Para abrir el proyecto en Android Studio: al abrirlo, Android Studio genera su
propio `local.properties` apuntando a tu SDK local (no está versionado, ver
`.gitignore`). No hace falta gradle wrapper manual — Android Studio lo gestiona
al sincronizar.

Repo publicado en `https://github.com/RoyAvanzaPRL/horas-trabajo` (público).

**Segunda ronda de features implementada (2026-07-24)**, tras sesión `/grill-me`:
tema claro/oscuro con selector manual (DataStore Preferences), pantalla de
Ajustes (tema + backup JSON, movido desde el menú de "Mis Trabajos"), campo
obligatorio `nombreUsuario` por Trabajo (con migración Room 1→2,
`ALTER TABLE ... DEFAULT ''`, y capacidad de editar un Trabajo ya creado), ese
nombre + pie de marca "Horas Trabajo — hecho por RoyPM" en el PDF/JPEG
exportado, y nueva navegación Trabajo → **AnioScreen** (año con flechas ◀▶ +
grid 3×4 de meses, mes actual resaltado) → MesScreen. Verificado compilando
(`assembleDebug` con BUILD SUCCESSFUL, incluyendo el schema v2 exportado por
Room) con el mismo JDK/SDK temporales. Un error real corregido en el camino:
`Icons.Filled.ChevronLeft/ChevronRight` no existen en el set básico de iconos
(harían falta `material-icons-extended`, pesado) — se sustituyó por texto
"‹"/"›", igual que el patrón ya usado en `MesScreen` para cambiar de mes.

## Decisiones — app de registro de horas de trabajo

Cerradas en sesión(es) `/grill-me` (2026-07-24):

### Alcance y plataforma
- **Usuarios:** single-user, sin cuentas ni multi-usuario.
  Razón: es solo para el usuario; auth/roles sería sobre-ingeniería para el caso actual.
- **Persistencia:** local-only, sin backend, sin sync entre dispositivos.
- **Plataforma:** Android nativo, exclusivamente. Sin iOS, sin PWA.
  Razón: el usuario solo tiene Android. Se evaluó compartir con un amigo con iPhone
  vía PWA (evita fricción de Apple Developer/TestFlight), pero se descartó por riesgo
  de borrado de IndexedDB/localStorage en iOS Safari tras inactividad prolongada
  (dato no confirmado al 100% para PWAs instaladas en pantalla de inicio). Se prefirió
  simplicidad Android-only, dejando al amigo con iPhone fuera por ahora.
- **Desarrollo:** el usuario no programa — se va a "vibecodear" con Claude (Sonnet
  high). Prioridad: código correcto e idiomático, SOLID/KISS, no "lo más fácil de
  aprender".

### Stack técnico
- **Lenguaje/UI:** Kotlin + Jetpack Compose (stack nativo recomendado por Google
  actualmente, frente a Java/XML legacy).
- **Almacenamiento primario:** Room (SQLite) como fuente de verdad. Los archivos
  (JSON/PDF/JPEG) son capas de export, no almacenamiento primario.

### Export / Import
- **JSON**: único formato de import/export real (round-trip fiable, sin pérdida de
  tipos). CSV y texto plano descartados como vía de import por ambigüedad de
  formato y falta de fiabilidad al reimportar.
- **PDF y JPEG**: resumen visual de solo lectura (no reimportable). PDF para
  documento formal/imprimible; JPEG para compartir rápido por WhatsApp a jefes
  (se ve la imagen al instante sin abrir un lector de PDF). Ambos son soporte
  nativo de Android, sin librerías pesadas (a diferencia de Excel/XLSX, que
  requeriría algo tipo Apache POI — descartado).
- **Contenido del resumen:** por un solo Trabajo a la vez (no combina trabajos
  distintos en un mismo documento). Incluye: día y hora de entrada/salida de cada
  entrada, suma de horas por día y total, suma de dinero por día y total, la
  tarifa por hora de ese mes, y el apartado de dinero extra del mes.

### Modelo de datos
- **Agrupación — entidad "Trabajo":** no hay categorías anidadas; una única
  entidad plana "Trabajo" (puede ser un empleo, un cliente, o cualquier cosa que
  el usuario quiera trackear). Cada entrada de horas pertenece a un Trabajo.
  **Los trabajos son completamente independientes entre sí — nunca se cruzan
  datos entre uno y otro** (ni tarifas, ni horas, ni resúmenes combinados).
- **Entrada de horas:** fecha + hora de entrada + hora de salida (la duración se
  calcula sola). Incluye un **checkbox "es del día siguiente"** para turnos que
  cruzan la medianoche (ej. entra 20:00, sale 02:00) — marcado a mano por el
  usuario, sin inferencia automática por comparación de horas (se descartó por
  preferencia explícita del usuario frente a una segunda fecha explícita).
- **Varias entradas por día permitidas** (ej. turno mañana Trabajo A + turno
  noche Trabajo B el mismo día). En la UI el hueco para una segunda entrada NO se
  muestra siempre — aparece como opción ("añadir otra entrada") solo cuando se
  necesita; el caso normal (una entrada) no se ve recargado.
- **Turno que cruza de mes** (ej. empieza 31 enero 23:00, termina 1 febrero
  03:00 con el checkbox de día siguiente marcado): las horas se asignan
  **enteras al mes en que empezó el turno**, sin reparto proporcional entre
  meses aunque las tarifas de ambos meses sean distintas.
- **Tarifa por hora:** fijada **por mes y por Trabajo** (no un valor único
  general del Trabajo). Razón: si cambia la tarifa, no debe recalcular meses
  pasados. Cada mes **hereda por defecto la tarifa del mes anterior**, editable
  si ese mes cambia el precio.
- **Dinero extra (por mes y por Trabajo):** varias entradas independientes,
  cada una con **cantidad + descripción corta + fecha**. La cantidad puede ser
  **negativa** (permite descuentos/adelantos), aunque el caso de uso real del
  usuario es siempre positivo (ej. "5€ reparto", repetible varias veces en el
  mismo mes con la misma descripción). Ejemplo de uso real: repartos ocasionales
  con propina que se suman al sueldo por horas de ese mes.
- **UI de registro:** vista tipo "plantilla del mes completo" con todos los días
  visibles, rellenando solo el día que interese (en vez de un flujo de "nueva
  entrada" con selector de fecha cada vez).
- **Notas por entrada:** campo de texto libre opcional, oculto por defecto en la
  UI (mismo patrón que la segunda entrada del día: solo aparece si se
  selecciona añadirlo, no se muestra siempre).
- **Moneda:** sin lógica real de divisas/i18n. Cada Trabajo tiene un **símbolo**
  de texto corto configurable (puramente visual, sin formateo ni conversión),
  con **€ como valor por defecto**.
- **Semanas dentro de la plantilla mensual:** se marca visualmente el inicio
  (lunes) y fin (domingo) de cada semana. En el último día de cada semana
  aparece una casilla resumen ("esta semana: X horas, X dinero"). Si la semana
  cruza el límite del mes, la casilla se muestra **parcial en el último día
  visible de ese mes** (ej. mes que corta en miércoles → parcial de esos 3
  días) y el mes siguiente muestra el parcial de los días restantes (ej.
  domingo con el parcial de los 4 días que faltaban). Sin reparto raro, cada
  día cuenta donde cae — mismo principio que el turno que cruza de mes.
- **Sin vista semanal separada** (la info semanal vive dentro de la vista
  mensual, no como pantalla aparte).
- **Sin recordatorios/notificaciones** para rellenar días — no se añade en el
  MVP; se revisita si en el uso real resulta ser un problema.

## Entorno de build

El JDK está en `/home/user/.jdks/jdk-17.0.20+8/bin/`. Para compilar el APK:

```bash
export JAVA_HOME=/home/user/.jdks/jdk-17.0.20+8
cd /home/user/github/cashtrack && ./gradlew :app:assembleDebug
```

El APK generado queda en `app/build/outputs/apk/debug/app-debug.apk`.

### Pendiente de grilling (aún sin resolver, no asumir)
- Edición/borrado de entradas pasadas: se asume CRUD estándar, no grillado
  explícitamente por ser funcionalidad básica esperada, no una decisión de diseño.

### Tema claro/oscuro, nombre por Trabajo y navegación por año/mes

Cerradas en sesión `/grill-me` (2026-07-24, segunda ronda):

- **Tema:** además de seguir el sistema (ya implementado por defecto), interruptor
  manual con 3 opciones: **Sistema (default) / Claro / Oscuro**, persistido.
  Vive en una **pantalla de Ajustes nueva**, dedicada.
- **Ajustes también absorbe el backup JSON** (export/import), que se saca del
  menú de overflow de "Mis Trabajos" y se mueve a esta pantalla nueva.
- **Nombre por Trabajo:** al crear un Trabajo se pide también el **nombre de la
  persona** (no un dato global de la app — es por Trabajo, decisión explícita
  pese a que iba a recomendar lo contrario). Campo **obligatorio**.
  Aparece en el PDF/JPEG del resumen exportado (ej. "Juan Pérez — Bar Pepe —
  Julio 2026").
- **Pie de marca en el resumen exportado:** texto `Horas Trabajo — hecho por
  RoyPM`, en **cada página** del PDF (no solo la última) y al final del JPEG.
- **Editar Trabajo:** se añade capacidad de editar un Trabajo ya creado (nombre
  del trabajo, nombre de la persona, símbolo de moneda) — no existía antes
  (solo crear/borrar). Razón: sin editar, corregir un typo obligaría a
  borrar-y-recrear, perdiendo horas/tarifas/dinero extra ya registrados por las
  foreign keys en cascada.
- **Navegación Trabajo → mes:** ya NO va directo al mes actual. Ahora entra en
  una **pantalla de año**: barra arriba con el año en curso y flechas ◀▶ para
  cambiar de año, y debajo una **cuadrícula 3×4** con los 12 meses de ese año.
  - Se muestran **todos los meses** (pasados, presente y futuros), no solo los
    que tienen datos.
  - El **mes actual se marca con color**.
  - Cada celda muestra **solo el nombre del mes** — sin resumen de horas/dinero
    ni indicador de "tiene datos" (se descartó explícitamente por ruido visual
    en una cuadrícula compacta).
  - Año por defecto al entrar: el año en curso.
  - Tocar un mes navega a la `MesScreen` ya existente, sin cambios en su
    comportamiento interno.
