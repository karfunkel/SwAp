@echo off
SET PROJECT_BASE=%~dp0%..
cd %~dp0%
start javaw -cp . -jar %~dp0%designer.jar
