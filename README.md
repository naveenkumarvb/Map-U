# Map-U
This sample App goes for selecting the location on mapview and custom name will be created for the location and also user can add notes for that location.

Prerequisites
Android SDK v26
Latest Android Build Tools
Android Support Repository
Getting started
This sample uses the Gradle build system.

Download the samples by cloning this repository or downloading an archived snapshot. (See the options at the top of the page.)
In Android Studio, create a new project and choose the "Import non-Android Studio project" or "Import Project" option.
Select the Map-U directory that you downloaded with this repository.
If prompted for a gradle configuration, accept the default settings. Alternatively use the "gradlew build" command to build the project directly.

Assumptions
If map is long clicked by the user -> custom location is added and saved with a green marker(pin).
If Pin or marker is clicked by user -> it will gets through you too Detail screen.In Detail screen you can add custom location name as label and also you can add notes for your selected location.
If info(title of the pin in map) of the particular pin or marker is clicked -> it will remove the selected location.
If favourite place button is clicked on the map-> it will take to you sorted list of selected and default locations.
Blue pin or marker will show you default locations where the data is recieved from the server.


