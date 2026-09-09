# P4 · Pasos y sueño

P4 reúne recuperación diaria en una pantalla que conserva el origen de cada registro y permite empezar sin una pulsera.

## Entrega

- Pasos y sueño por fecha, con historial local y actualización por registro.
- Entrada manual con validación de unidades: pasos enteros y sueño en horas convertido a minutos.
- Procedencia visible (`Manual` o `Health Connect`) para no mezclar datos sin explicación.
- Acceso a la configuración de Health Connect desde Deporte, preparado para conceder el permiso a la app de pulsera o teléfono.
- Regreso a la app sin borrar registros manuales.

## Alcance real de la conexión

El proyecto no incorpora todavía el SDK de lectura de Health Connect porque el entorno actual no contiene sus artefactos y la lectura requiere validar permisos y un dispositivo compatible. La pantalla ya tiene el punto de entrada y el modelo de procedencia; el siguiente paquete debe añadir el cliente oficial, pedir `READ_STEPS`/`READ_SLEEP` y consultar agregados con la convención de fecha visible. No se presenta la entrada manual como sincronización automática.
