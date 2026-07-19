#!/usr/bin/env bash

cd "$(dirname "$0")"

JAVAFX_MODULE_PATH="${JAVAFX_MODULE_PATH:-$HOME/javafx-sdk-17.0.19/lib}"

java --module-path "$JAVAFX_MODULE_PATH" \
	--add-modules javafx.controls,javafx.base,javafx.graphics,javafx.fxml \
	--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
	--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
	--add-exports javafx.base/com.sun.javafx.binding=ALL-UNNAMED \
	--add-exports javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED \
	-jar OpenConsignment.jar
