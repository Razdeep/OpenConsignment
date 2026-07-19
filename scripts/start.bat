set JAVA_HOME=C:\Program Files\Java\jdk-17\lib
set PATH=C:\Program Files\Java\jdk-17\bin
set JAVAFX_MODULE_PATH=%USERPROFILE%\javafx-sdk-17.0.18\lib

echo %JAVAFX_MODULE_PATH%

java --module-path "%JAVAFX_MODULE_PATH%" ^
--add-modules javafx.controls,javafx.base,javafx.graphics,javafx.fxml ^
--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
--add-exports=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED ^
--add-exports=javafx.base/com.sun.javafx.binding=ALL-UNNAMED ^
--add-exports=javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED ^
-jar OpenConsignment.jar

pause