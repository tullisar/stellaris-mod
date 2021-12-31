# LibTullisar: Core Package Requirements

## Introduction

This file describes the requirements for the Core package of the library's functions. 

## Utilities

### Library State
When the `on_game_start` event is fired, the library **shall** $\req{00}{00001}{0}$ perform library initialization as follows:
1. Initialize the ascension perk combo system per *`SRD_01_00002_0`* 
2. $\req{00}{00020}{0}$ Set a global flag indicating that the mod has been installed.

The library **shall** $\req{00}{00002}{0}$ provide a scripted trigger that can be used to check whether the mod has been installed. 

When the `on_game_start_country` event is fired for a scoped country, the library **shall** $\req{00}{00009}{0}$ perform library initialization for the scoped country as follows:
1. Do a thing
2. $\req{00}{00019}{0}$ Set a country flag indicating that the library has been initialized for the scoped country.

The library **shall** $\req{00}{00008}{0}$ provide a scripted trigger to determine whether library initialization has completed for a scoped country.

### Math
The library **shall** $\req{00}{00010}{0}$ provide a scripted effect which facilitates calling `set_variable` on the `event_target:global_event_country` scope. 

The library **shall** $\req{00}{00011}{0}$ provide a scripted effect which facilitates calling `change_varible` on the `event_target:global_event_country` scope. 

The library **shall** $\req{00}{00012}{0}$ provide a scripted trigger which facilitates calling `is_variable_set` on the `event_target:global_event_country` scope.

The library **shall** $\req{00}{00013}{0}$ provide a scripted trigger which facilitates calling `check_variable` for the `=` operator on the `event_target:global_event_country` scope.

The library **shall** $\req{00}{00018}{0}$ provide a scripted trigger which facilitates calling an inverted `check_variable` for the `=` operator on the `event_target:global_event_country` scope.

The library **shall** $\req{00}{00014}{0}$ provide a scripted trigger which facilitates calling `check_variable` for the `>` operator on the `event_target:global_event_country` scope.

The library **shall** $\req{00}{00015}{0}$ provide a scripted trigger which facilitates calling `check_variable` for the `<` operator on the `event_target:global_event_country` scope.

The library **shall** $\req{00}{00016}{0}$ provide a scripted trigger which facilitates calling `check_variable` for the `>=` operator on the `event_target:global_event_country` scope.

The library **shall** $\req{00}{00017}{0}$ provide a scripted trigger which facilitates calling `check_variable` for the `<=` operator on the `event_target:global_event_country` scope.

### Logging

TODO(tullisar)

## Multiplayer

When the `on_game_start_country` event is fired, the library **shall** $\req{00}{00003}{0}$ set a flag indicating that the first playable country processed by this event is the multiplayer host.

The library **shall** $\req{00}{00004}{0}$ provide a scripted trigger that can be used to check whether the multiplayer host has been set.

The library **shall** $\req{00}{00005}{0}$ provide a scripted trigger that can be used to check whether a scoped country is the multiplayer host.

The library **shall** $\req{00}{00006}{0}$ provide a scripted effect that can be used to set a scoped country controlled by a player as the multiplayer host.

## Diagnostics

The library **shall** $\req{00}{00007}{0}$ provide a scripted trigger to determine whether verbose logging is enabled.