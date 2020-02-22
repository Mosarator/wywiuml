# wywiuml ("what you write is UML")<br> A WYSIWYG UML-Editor

:::info
Work in progress! Not a finished product.
:::

## Goal

The aim of this project is to create a minimalistic UML editor that follows the wysiwyg concept and is suitable for use in school. The user should be able to make errors, but also be notified about those.

Only Class Diagrams are possible at the moment.

## Functionality (currently)

 - create and move classes
     - edit classes via doubleclick
     - make classes abstract
     - switch class to interface
 - draw associations between classes
     - switch association type to aggregation or composition
     - edit variable on association, via doubleclick
     - edit multiplicity
 - draw generalization/realization
 - generate code from Diagramm
 - export diagram as imagefile
 - export diagram as savefile
 - import diagram from savefile
 - import class from java file
 - import classes from java project

... and some more

## Currently unsupported 
many things, but most knowingly:
 - no way to declare abstract methods
 - no way to declare static methods
 - if abstract or static members are imported with the class, they will be displayed as normal, especially while editing
 - no way do declare generics, even though you can import them.
 - code loss of imported classes if they were edited
 - code won't be saved in uml savefiles


# Credits and Dependencies

## Codebase
This project started as a Fork of https://www.github.com/AwenHuang/UML-Editor/ but received close to a total Code-overhaul. What is left is the general structure of the packages and classes.

## Dependencies
This project uses Maven for its dependencies. Check the POM file for more information.