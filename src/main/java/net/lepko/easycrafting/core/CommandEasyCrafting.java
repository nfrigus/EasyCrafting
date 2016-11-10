package net.lepko.easycrafting.core;

import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CommandEasyCrafting extends CommandBase {
	
    @Override
    public String getCommandName() {
        return "easycrafting";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return TextFormatting.YELLOW + "/" + getCommandName() + " [version | recursion]";
    }


    @Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (args[0].equalsIgnoreCase("version")) {
            processVersionCommand(sender);
        } else if (args[0].equalsIgnoreCase("recursion")) {
            processRecursionCommand(sender, args);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
    
    private void tell(ICommandSender sender, String message, TextFormatting colour) {
    	TextComponentString str=new TextComponentString(message);
    	sender.addChatMessage(str.setStyle(
    			str.getStyle()
    			.createShallowCopy()
    			.setColor(colour)));
    }

    private void processRecursionCommand(ICommandSender sender, String[] args) {
        if (args.length == 1) {
        	tell(sender, " Recursion is: " + ConfigHandler.MAX_RECURSION, TextFormatting.AQUA);
        } else if (args.length == 2) {
        	int number=0;
        	try{
        		int i = Integer.parseInt(args[1]);
        		if(i<0 || i>10) {
        			tell(sender, "Recursion limit must be between 0 and 10, got "+args[1], TextFormatting.RED);
        			return;
        		}
        		number=i;
        	}catch(NumberFormatException e){
        		tell(sender, "Recursion limit must be an integer, got "+args[1], TextFormatting.RED);
        		return;
        	}
            ConfigHandler.setRecursion(number);
            tell(sender,  "> Recursion set: " + number, TextFormatting.YELLOW);
        } else {
        	 tell(sender, "Usage:", TextFormatting.RED);  
        	 tell(sender,  "  /easycrafting recursion - Get current recursion value",TextFormatting.RED);
        	 tell(sender,  "  /easycrafting recursion <value> - Set new recursion value,",TextFormatting.RED);
        }
    }

	private void processVersionCommand(ICommandSender sender) {
    	tell(sender, "> " + Ref.MOD_NAME + " version " + Ref.VERSION, TextFormatting.YELLOW);
    }

}
