A Spigot warp plugin.

Commands:
  - /warp <name>
  - /warp list
  - /warp gui
  - /warp help
  - /warp create <Name> <MaterialID> <ColorCode> <SlotID> <GUIVisibility>
  - /warp remove <Name>
  
Permissions:
  - permissions:
      - dwarp.warp:
          - Allows a player to warp to a warp location and open the warp gui.
      - dwarp.list:
          - Get a List of all available warp locations.
      - dwarp.create:
          - Allows a player to create warp locations.
      - dwarp.remove:  
          - Allows a player to remove warp locations.
      - dwarp.*:       
          - Allows a player to use the plugin completely.
