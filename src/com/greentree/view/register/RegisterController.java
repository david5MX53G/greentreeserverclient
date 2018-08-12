package com.greentree.view.register;

import com.greentree.controller.GreenTreeController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.interfaces.RSAPublicKey;

import javax.swing.JFileChooser;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides handlers for <code>{@link RegisterJFrame}</code>
 *
 * @author david5MX53G
 *
 */
public class RegisterController implements ActionListener {

    /**
     * {@link org.apache.logging.log4j.Logger} is for logging logs to the log
     */
    private static final Logger LOG = LogManager.getLogger();
    
    /** This identifies which method is logging messages */
    private static String methodName = "unknown method ";
    
    /**
     * <code>{@link com.greentree.view.RegisterJFrame}</code> object sending
     * events to the {@link
     * ActionListener} of this {@link RegisterController}
     */
    private final RegisterJInternalFrame regJInternalFrame;

    /**
     * this {@link JDesktopPane} is used to display warnings, errors, info, etc.
     */
    private final JDesktopPane mainDesktop;

    /**
     * builds a new {@link RegisterController} to handle events from the given <code>
     * RegisterJInternalFrame</code>
     *
     * @param iFrame {@link RegisterJInternalFrame} sending events to this
     * controller
     */
    public RegisterController(RegisterJInternalFrame iFrame, JDesktopPane mainDesktop) {
        methodName = "RegisterController(RegisterJInternalFrame, JDesktopPane) ";
        this.regJInternalFrame = iFrame;
        this.regJInternalFrame.getCancelBtn().addActionListener(this);
        this.regJInternalFrame.getSubmitBtn().addActionListener(this);
        this.mainDesktop = mainDesktop;
        LOG.debug(methodName + "done");
    }

    /**
     * Invokes one of the <code>{@link RegisterController}</code> methods based
     * on the <code>
     * {@link RegisterJFrame}</code> element sending the
     * <code>{@link ActionEvent}</code>
     *
     * @param e <code>ActionEvent</code> used to determine which
     * <code>TokenPrompt</code> element sent the <code>ActionEvent</code>
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        LOG.debug(methodName + e.getActionCommand());

        /**
         * if this was registered with a RegisterJInternalFrame, use
         * RegisterJInternalFrame methods this is a holdover from the days when
         * RegisterJFrame existed, but it's still a good sanity check.
         */
        if (this.regJInternalFrame instanceof RegisterJInternalFrame) {
            // fire the cancel button action when the cancel button is clicked
            if (e.getSource().equals(regJInternalFrame.getCancelBtn())) {
                cancelButton_ActionPerformed(e);
            }

            // fire the submit button action when the submit button is clicked
            if ("Submit".equals(e.getActionCommand())) {
                submitBtn_actionPerformed(e);
            }
        }
    }

    /**
     * Invokes
     * {@link com.greentree.model.business.manager.GreenTreeManager#registerToken(String)}
     * using a {@link String} constructed from the value of
     * {@link RegisterJFrame#getPass()}.
     *
     * TODO: replace
     * {@link com.greentree.model.business.manager.GreenTreeManager#registerToken(String)}
     * with
     * {@link com.greentree.model.business.manager.GreenTreeManager#registerToken(char[])}
     *
     * @param e <code>{@link ActionEvent}</code> should be from <code>{@link
     * RegisterJFrame#getSubmitBtn()}</code> <code>JButton</code>
     */
    private void submitBtn_actionPerformed(ActionEvent ev) {
        methodName = "submitBtn_actionPerformed(ActionEvent) ";
        String msg;
        GreenTreeController ctrl = GreenTreeController.getInstance();
        
        if (ctrl instanceof GreenTreeController) {
            LOG.debug(methodName + "GreenTreeController initialized");
        } else {
            LOG.error(methodName + "failed to get GreenTreeController");
        }

        // register GreenTreeManager Token
        if (!ctrl.registerToken(new String(regJInternalFrame.getPass()))) {
            LOG.error(methodName + 
                "GreenTreeManager.registerToken(String) failed");

            JOptionPane.showInternalMessageDialog(
                mainDesktop,
                "Failed to save new token",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        } 

        // return RSAPublicKey for user to save and use in future auth events
        else {
            LOG.debug("GreenTreeManager.registerToken(String) succeeded");
            RSAPublicKey key = ctrl.getPublicKey();
            
            if (key instanceof RSAPublicKey) {
                LOG.debug(methodName + "GreenTreeController.getPublicKey() "
                    + "returned RSAPublicKey");
            } else {
                LOG.error(methodName + "GreenTreeController.getPublicKey() "
                    + "did not return RSAPublicKey");
            }
            
            JFileChooser fc = new JFileChooser();
            int chooserState;

            chooserState = fc.showSaveDialog(this.regJInternalFrame);

            if (chooserState == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String filePath = file.getAbsolutePath();
                LOG.debug(methodName + "saving RSAPublicKey to: " + filePath);

                // write the public key to the file chosen by the user
                try (ObjectOutputStream out
                    = new ObjectOutputStream(new FileOutputStream(filePath))) {
                    out.writeObject(key);
                } catch (IOException ex) {                    
                    LOG.debug("FileOutputStream(" + filePath + ") " 
                        + ex.getMessage());
                    
                    JOptionPane.showInternalMessageDialog(
                        mainDesktop,
                        "Failed saving to filesystem",
                        ex.getClass().getName(),
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                LOG.debug(methodName + "JFileChooser cancelled by user.");
            }

            this.regJInternalFrame.dispose();
        }
    }

    private void cancelButton_ActionPerformed(ActionEvent ev) {
        if (this.regJInternalFrame instanceof RegisterJInternalFrame) {
            this.regJInternalFrame.dispose();
        }
        LOG.debug("cancelButton_ActionPerformed(ActionEvent) PASSED");
    }
}
