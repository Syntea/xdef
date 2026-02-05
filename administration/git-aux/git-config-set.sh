#!/bin/bash
#doporucene nastaveni git-u

#for OS Windows
#==============

#allow symlinks
# - also in advance must be set OS-Windows „Developer Mode“ (Start > Settings > Update & Security >
#   For Developers > Developer Mode > On)
# - and then maybe required to refresh symlinks in cloned git-repo >git reset --hard
# - maybe set core.symlinks locally, that overwrites global settings, than do: git config --unset core.symlinks
git config --global core.symlinks true

#set editor to edit commit-messages
git config --global core.editor notepad
