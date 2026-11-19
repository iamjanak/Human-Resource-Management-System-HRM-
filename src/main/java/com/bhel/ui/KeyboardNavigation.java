package com.bhel.ui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Shared keyboard navigation for HRM windows. */
public final class KeyboardNavigation {

    private KeyboardNavigation() {
    }

    public static void installEscape(JFrame frame, Runnable action) {
        installEscape(frame.getRootPane(), action);
    }

    public static void installEscape(JDialog dialog, Runnable action) {
        installEscape(dialog.getRootPane(), action);
    }

    private static void installEscape(JRootPane rootPane, Runnable action) {
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        rootPane.getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }
}
