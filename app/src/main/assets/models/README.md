# Modelos 3D de las mascotas

La APK usa directamente el modelo:

- `conejitos_pixar.glb` — contiene los 4 conejitos en un único archivo.

La pantalla de la mascota carga este GLB con SceneView/Filament y reproduce las animaciones glTF incluidas en el modelo. El fondo cambia por etapas según el nivel.

Si en el futuro se quieren seleccionar los 4 conejitos de forma independiente, lo ideal es exportarlos como cuatro GLB separados o preparar el archivo con variantes/animaciones identificables.
