# P2 · Agenda y organización

P2 convierte el área Agenda en una mesa de trabajo local para registrar y revisar la vida cotidiana sin depender de una cuenta.

## Entrega

- Vista de hoy con una franja de fechas, prioridades y acciones pendientes.
- Semana, Bandeja y Proyectos como vistas del mismo conjunto de elementos.
- Tareas, eventos, recordatorios y hábitos, con fecha y hora opcionales.
- Captura rápida sin fecha para no interrumpir una idea; después se puede revisar desde la bandeja.
- Proyectos con objetivo escrito y porcentaje de tareas completadas.
- Persistencia en `SharedPreferences` mediante Gson, con borrado de elementos y proyectos.
- Avisos locales para recordatorios futuros mediante `AlarmManager` y un canal de notificaciones.
- Selector de áreas conectado: Agenda abre esta pantalla desde Home y conserva el análisis existente.

## Decisiones de calidad

Los datos propios se guardan en el dispositivo y la interfaz muestra estados vacíos y errores de entrada. Una hora se valida como `HH:mm` antes de programar un aviso. Las notificaciones necesitan el permiso del sistema en Android 13 o posterior; la aplicación no interpreta un recordatorio como un evento del calendario externo.

La recurrencia avanzada, calendarios externos, colaboración y lenguaje natural quedan fuera de P2. El modelo deja espacio para añadirlas sin duplicar tareas existentes.
