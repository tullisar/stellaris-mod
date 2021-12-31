# LibTullisar: Ascension Perk Combo System Requirements

## Table of Contents
- [Introduction](#introduction)
- [Utilities](#utilities)
   - [Library Initialization](#library-initialization)

## Introduction

$\color{yellow}TBD$

## Utilities

### Library Initialization

The library **shall** $\req{01}{00001}{0}$ provide a scripted trigger to determine whether the ascension perk combo feature is enabled.

When requested during library initialization and the ascension perk combo feature is enabled, the library **shall** $\req{01}{00002}{0}$ initialize the ascension perk combo feature. This has the following effects:

1. $\req{01}{00004}{0}$ Register the following ascension perk types with the ascension perk combo feature (per $\refreq{01}{00005}{0}$):
   - $\req{01}{00006}{0}$  $\enum{government}$
   - $\req{01}{00007}{0}$  $\enum{military}$
   - $\req{01}{00008}{0}$  $\enum{diplomacy}$
   - $\req{01}{00009}{0}$  $\enum{mega\_engineering}$
   - $\req{01}{00010}{0}$  $\enum{research}$
   - $\req{01}{00011}{0}$  $\enum{economy}$
   - $\req{01}{00012}{0}$  $\enum{espionage}$
   - $\req{01}{00013}{0}$  $\enum{expansion}$
   - $\req{01}{00014}{0}$  $\enum{exploration}$
2. $\req{01}{00015}{0}$ Fire the on action $\onaction{\globalscope}{\verb!tul_on_ap_combo_register_mod_type!}$ to allow other mods to register additional types with the feature (per $\refreq{01}{00005}{0}$).
3. $\req{01}{00017}{0}$ Register types for all supported ascension perks, as described below (per $\refreq{01}{00016}{0}$):
   | Requirement | Perk Key                               | Types             |
   |-------------|----------------------------------------|-------------------|
   | TBD         |$\keyref{ap\_executive\_vigor}$         |$\enum{government}$|
   | TBD         |$\keyref{ap\_transcendent\_learning}$   |$\enum{government}$|
   | TBD         |$\keyref{ap\_imperial\_prerogative}$    |$\enum{government}$|
4. $\req{01}{00019}{0}$ Fire the on action $\onaction{\globalscope}{\verb!tul_on_ap_combo_register_mod_perk_association!}$ to allow other mods to register additional types with the feature (per $\refreq{01}{00016}{0}$).
   

The library **shall** $\req{01}{00005}{0}$ provivde a scripted effect that allows new ascension perk combo types to be registered during library initialization.

The library **shall** $\req{01}{00003}{0}$ provide a scripted trigger to determine whether the ascension perk combo feature has been initialized.

The library **shall** $\req{01}{00016}{0}$ provide a scripted effect that allows a combo type to be associated with a specified ascension perk.

The library **shall** $\req{01}{00018}{0}$ provide a scripted trigger to determine whether a given ascension perk type has been registered.

## Combo Bonus Management

When the $\onaction{\countryscope}{\verb!on_ascension_perk_picked!}$ event is fired, the library **shall** $\req{01}{00020}{0}$ update the ascension perk combo bonus modifiers for each ascension perk the scoped country has active. Updating modifiers happens as follows:

1. TBD