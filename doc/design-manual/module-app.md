# Module "app"

## Overview

The `app` module implements a simple application framework. Its central `AbstractDesktopApplication` class on the JVM platform can be started with command line options, loads and stores user preferences, and defines an `ApplicationData` class and various `Actions` like `OpenFileAction` or `SaveFileAction` to make it persistent.

The module structures an application in three main parts.

Application
: The `Application` object is created and started from the main program. It holds the `ApplicationDataViewController`, which acts as `ApplicationDataHolder` by holding the current `ApplicationData`, and (in case of the JVM platform) creates an `ApplicationFrame` that make the main application `Actions` like `OpenFileAction` accessible to the user as menu items or push buttons.

A concrete `Application` on the JVM platform like `AntaresSwing` extends `AbstractDesktopApplicationSwing` and offers a `main()` method to start it. 

ApplicationData and its Savable
: The `ApplicationDate` class combines the application's main `Storable` (e.g. the current circuit) and the corresponding `Savable`, which contains information about where the `Storable` is to be saved (e.g. a file name and path).

The current `ApplicationData` is held by an `ApplicationDataHolder` that posts `ApplicationDataEvents` on the system's `EventBus` when the current `ApplicationData` has changed.

ApplicationDataView
: `ApplicationDataView` is a Kotlin-common representation of an `Application`'s UI, divided in a UIController and a UIView. The `ApplicationDataViewController` acts as `ApplicationDataHolder`, offers methods like `newData()`, `open()` or `save()`, and coordinates various ways of closing unsaved data by using the `ApplicationDataView` to ask the user "Do you want to save before closing?".

## Settings

The `Settings` object accessible in `BaseModule.settings` contains settings the user changes while using the `Application`, such as position and size of the main application window. These `Settings` are made persistent and are re-established the next time the application is used. Client classes implement the `base.Disposable.dispose()` method called by its owner to set setting values e.g. with `Settings.set(String, Any)`, and typically call `Settings.getInt(String)` in their constructor to retrieve `Settings` values.

## Properties

The `Properties` object accessible in `BaseModule.properties` contains system-wide available properties defined as name-value pairs. Properties are initially established by the `Application` at startup in a `Module`, e.g. in the `DrawModule` to set the default zoom step value to 1.5f. This value is the used e.g. by `AbstractZoomPanAction` to control how much the zoom factor is to be changed by a single mouse wheel action.

## Preferences

`Preferences` are special `Properties` the user can change in the "File -> User preferences" dialog on the JVM platform.

## ApplicationFrame

The `ApplicationFrame` is the main `ApplicationWindow` of a Jabbah `Application` on the JVM platform. It uses `Settings` to make its position and size persistent, uses `MenuBarBuilder` to present the `Application`'s main `Actions` as menu items in a menu bar, has a `Toolbar` at its top border, and displays the `Application`'s main UI as its content

