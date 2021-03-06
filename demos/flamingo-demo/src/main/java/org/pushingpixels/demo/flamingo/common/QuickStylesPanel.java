/**
 *
 */
package org.pushingpixels.demo.flamingo.common;

import org.pushingpixels.demo.flamingo.svg.tango.transcoded.*;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.DecoratedResizableIcon;
import org.pushingpixels.flamingo.api.common.model.*;
import org.pushingpixels.neon.icon.*;

import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

public class QuickStylesPanel {
    public static CommandPanelContentModel getQuickStylesContentModel(
            ResourceBundle resourceBundle, Locale locale) {
        List<CommandGroup> commandGroups = new ArrayList<>();

        MessageFormat mf = new MessageFormat(resourceBundle.getString("PanelStyles.text"));
        mf.setLocale(locale);

        for (int groupIndex = 0; groupIndex < 4; groupIndex++) {
            String commandGroupName = mf.format(new Object[] { groupIndex });
            List<Command> commands = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                final String deco = groupIndex + "/" + i;
                ResizableIcon.Factory iconFactory = DecoratedResizableIcon.factory(
                        Font_x_generic.factory(),
                        (Component c, Graphics g, int x, int y,
                                int width, int height) -> {
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setColor(Color.black);
                            if (c.getComponentOrientation().isLeftToRight()) {
                                g2d.drawString(deco, x + 2, y + height - 2);
                            } else {
                                g2d.drawString(deco,
                                        x + width - g2d.getFontMetrics().stringWidth(deco) - 2,
                                        y + height - 2);
                            }
                            g2d.dispose();
                        });
                Command command = Command.builder()
                        .setIconFactory(iconFactory)
                        .setToggle()
                        .setAction((CommandActionEvent e) ->
                                System.out.println("Invoked action on " + deco))
                        .build();
                commands.add(command);
            }

            commandGroups.add(new CommandGroup(commandGroupName, commands));
        }

        CommandPanelContentModel commandPanelContentModel =
                new CommandPanelContentModel(commandGroups);
        commandPanelContentModel.setSingleSelectionMode(true);

        return commandPanelContentModel;
    }

    public static void updatePanelContentModel(CommandPanelContentModel model,
            ResourceBundle resourceBundle, Locale locale) {
        MessageFormat mf = new MessageFormat(resourceBundle.getString("PanelStyles.text"));
        mf.setLocale(locale);

        List<CommandGroup> commandGroups = model.getCommandGroups();
        for (int groupIndex = 0; groupIndex < commandGroups.size(); groupIndex++) {
            CommandGroup commandGroup = commandGroups.get(groupIndex);
            String commandGroupName = mf.format(new Object[] { groupIndex });
            commandGroup.setTitle(commandGroupName);
        }
    }

    public static CommandPanelPresentationModel getQuickStylesPresentationModel() {
        return CommandPanelPresentationModel.builder()
                .setToShowGroupLabels(false)
                .setCommandPresentationState(CommandButtonPresentationState.FIT_TO_ICON)
                .setCommandIconDimension(48)
                .build();
    }
}