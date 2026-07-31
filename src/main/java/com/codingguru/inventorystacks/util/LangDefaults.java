package com.codingguru.inventorystacks.util;

public final class LangDefaults {
	
	private LangDefaults() {
	}
	
    public static final String GIVEN_ITEM = "Gave %amount% [%item%] to %id%";
    public static final String HAND_ITEMS_STACKED = "&aYou have successfully stacked all items in your hand.";
    public static final String ALL_ITEMS_STACKED = "&aYou have successfully stacked all items in your inventory.";
    public static final String INVALID_STACK_TYPE = "&c%type% is not a valid stack type. Use 'HAND' or 'ALL'.";
    public static final String DISALLOW_ANVIL_STACK = "&cYou cannot use this stacked item here.";
    public static final String PREVENT_SHIFT_COMBINING_DAMAGEABLE_ITEMS = "&cYou cannot combine this item WITH SHIFT due to a durability item glitch.";
    public static final String COMMAND_DISABLED = "&cThis command has been disabled.";
    public static final String BUNDLE_FIX = "&cYou cannot put stacked damageable items into bundles due to client limitations.";
    public static final String RELOAD = "&aYou have successfully reloaded all configuration files.";
    public static final String INCORRECT_USAGE = "&cCorrect Usage: %command%";
    public static final String IN_GAME_ONLY = "&cYou can only execute this in game.";
    public static final String NUMBER_EXCEPTION = "&cYou must enter a correct amount.";
    public static final String PLAYER_NOT_FOUND = "&cNo entity was found with the id: %id%";
    public static final String NO_PERMISSION = "&cYou do not have permission to execute this command.";
    
}