package com.greentree.view.addmsg;

import com.greentree.controller.GreenTreeController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.InputMismatchException;

import javax.swing.JFileChooser;

import com.greentree.view.main.MainJFrameController;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <code>AddMsgJInternalController</code> consumes events from the {@link
 * AddMsgJFrame}.
 *
 * @author david5MX53G
 *
 */
public class AddMsgJInternalController implements ActionListener {

    /**
     * {@link org.apache.logging.log4j.Logger} is for logging logs to the log
     */
    Logger logger = LogManager.getLogger();

    /**
     * {@link AddMsgJFrame} sending events to this controller
     */
    private AddMsgJInternalFrame iFrame;

    /**
     * {@link GreenTreeManager} for managing data objects
     */
    private final GreenTreeController manager = GreenTreeController.getInstance();

    /**
     * {@link AddMsgJFrame} sends events to this controller
     */
    private MainJFrameController mainCtrl;

    /**
     * {@link RSAPublicKey} which is given read access to the message
     */
    private RSAPublicKey toKey;

    /**
     * @param iFrame {@link AddMsgJFrame} sends events to this
     * {@link AddMsgJInternalController}
     * @param main {@link MainJFrameController} which provides the active
     * session key
     */
    public AddMsgJInternalController(
        AddMsgJInternalFrame iFrame, MainJFrameController main
    ) {
        if (main.getKey() == null) {
            JOptionPane.showInternalMessageDialog(
                main.getDesktop().getDesktopPane(),
                "must be authenticated first",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            logger.error("must be authenticated first");
        } else {
            this.iFrame = iFrame;
            this.mainCtrl = main;
            iFrame.getKeyBtn().addActionListener(this);
            iFrame.getSubmitBtn().addActionListener(this);
            logger.debug("initialized");
        }
    }

    @Override
    public void actionPerformed(ActionEvent aev) {
        if (aev.getSource().equals(iFrame.getKeyBtn())) {
            keyBtnAction();
        };

        if (aev.getSource().equals(iFrame.getSubmitBtn())) {
            submitBtnAction();
        };
    }

    /**
     * marshals data from message fields and adds a
     * {@link com.greentree.model.domain.Block}
     */
    private void submitBtnAction() {
        String diagTitle = null;
        String diagText = null;
        GregorianCalendar expires = new GregorianCalendar();
        String msg = this.iFrame.getMsgFld();
        boolean success;

        if (this.iFrame.getHours() > 0) {
            expires.add(GregorianCalendar.HOUR, this.iFrame.getHours());
        } else {
            JOptionPane.showInternalMessageDialog(
                this.mainCtrl.getDesktop().getDesktopPane(),
                "invalid time input",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        success = manager.registerService("TokenService");
        if (!success) {
            JOptionPane.showInternalMessageDialog(
                this.mainCtrl.getDesktop().getDesktopPane(),
                "failed to register storage service",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        try {
            if (!manager.registerToken(mainCtrl.getKey(), mainCtrl.getCiphertext())) {
                JOptionPane.showInternalMessageDialog(
                    this.mainCtrl.getDesktop().getDesktopPane(),
                    "failed to retrieve authenticated token",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } else if (!manager.addBlock(msg, toKey, new Date().getTime(), expires.getTimeInMillis())) {
                JOptionPane.showInternalMessageDialog(
                    this.mainCtrl.getDesktop().getDesktopPane(),
                    "failed to add message",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                logger.debug("added message, '" + msg + "'");
                diagTitle = "Success";
                diagText = "Message added";
            }
        } catch (InputMismatchException ex) {
            logger.debug(ex.getMessage());
            diagTitle = ex.getClass().getSimpleName();
            diagText = ex.getMessage();
        }
        JOptionPane.showInternalMessageDialog(
            this.mainCtrl.getDesktop().getDesktopPane(),
            diagText,
            diagTitle,
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void keyBtnAction() {
        JFileChooser fc = new JFileChooser();

        // prompt for a file and store the resulting return state
        int returnVal = fc.showOpenDialog(iFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            logger.debug("opening " + file.getName());

            // load a new frame with the log messages from the chosen key
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
                toKey = (RSAPublicKey) input.readObject();
                logger.debug("RSAPublicKey successfully parsed");
            } catch (Exception ex) {
                logger.debug(ex.getMessage());
            }
        }
    }
}
