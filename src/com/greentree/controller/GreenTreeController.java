/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.greentree.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.out;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This makes a {@link java.net.Socket} connection to a {@link
 * com.greentree.model.business.manager.GreenTreeManager} on a
 * <code>GreenTreeServer</code> and passes data between it and a
 * <code>GreenTreeServerClient</code>.
 *
 * @author david5MX53G
 */
public class GreenTreeController {

    /**
     * This {@link org.apache.logging.log4j.Logger} logs logs to the log.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * This stores the Singleton of this class.
     */
    private static GreenTreeController instance_;

    /**
     * This provides params for methods to open new {@link java.net.Socket}
     * connections with a remote GreenTreeServer.
     *
     * TODO: move these params into a config file accessed via a Properties obj
     */
    private static InetAddress inetAddr;
    private static final int PORT = 8189;
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    /**
     * This instantiates the {@link GreenTreeController#instance_} of this
     * class.
     */
    private GreenTreeController() {
        try {
            inetAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            LOG.error("constructor threw " + ex.getClass().getSimpleName()
                + ": " + ex.getMessage());
        }
        LOG.debug("GreenTreeController() complete");
    }

    /**
     * @return Singleton {@link GreenTreeController#instance_} of this class
     */
    public static GreenTreeController getInstance() {
        if (instance_ == null) {
            instance_ = new GreenTreeController();
        }

        LOG.debug("getInstance() returned "
            + instance_.getClass().getSimpleName());

        return instance_;
    }

    /**
     * This closes any extant {@link GreenTreeController#socket} and returns a
     * fresh one.
     *
     * @return {@link java.net.Socket}
     */
    private Socket getSocket() {
        LOG.debug("Socket getSocket() started");
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(inetAddr, PORT);
            }

            if (socket instanceof Socket) {
                LOG.debug("Socket getSocket() succeeded");
            } else {
                LOG.error("Socket getSocket() failed");
            }
        } catch (IOException ex) {
            LOG.error("Socket getSocket() threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return socket;
    }

    /**
     * This closes the {@link GreenTreeController#socket} so that other methods
     * don't have to handle this. This also closes any {@link
     * java.io.ObjectInputStream} of the <code>Socket</code>.
     *
     * @return true, if the <code>Socket</code> closes successfully
     */
    private boolean closeSocket() {
        boolean result;
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
            result = true;
        } catch (NullPointerException | IOException ex) {
            result = false;
            LOG.error("boolean closeSocket() threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        LOG.debug("closeSocket() returned " + String.valueOf(result));
        return result;
    }

    /**
     * This closes any extant {@link GreenTreeController#out} and returns a
     * fresh one, flushed and ready for anything!
     *
     * @return {@link java.io.ObjectOutputStream}
     */
    private ObjectOutputStream getOut() {
        LOG.debug("getOut() started");
        try {
            socket = this.getSocket();
            LOG.debug("socket = " + socket.getClass().getSimpleName());

            if (out != null) {
                out.close();
                out = null;
                LOG.debug("ObjectOutputStream closed and set to null");
            }

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            if (out instanceof ObjectOutputStream) {
                LOG.debug("out instanceof ObjectOutputStream = "
                    + String.valueOf(out instanceof ObjectOutputStream));
            } else {
                throw new IOException(this.getClass().getSimpleName()
                    + " ObjectOutputStream getOut() failed");
            }

        } catch (IOException ex) {
            LOG.error("ObjectOutputStream getOut() threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        LOG.debug("ObjectOutputStream getOut() done");
        return out;
    }

    /**
     * This closes any extant {@link GreenTreeController#out} and returns a
     * fresh one, flushed and ready for anything!
     *
     * @return {@link java.io.ObjectOutputStream}
     */
    private ObjectInputStream getIn() {
        LOG.debug("getIn() started");
        try {
            socket = this.getSocket();
            if (in != null) {
                in.close();
                in = null;
            }
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            LOG.error("ObjectInputStream getIn() threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return in;
    }

    /**
     * Initializes a <code>Properties</code> object for acquiring runtime config
     * settings.
     *
     * @return <code>true</code> for success, <code>false</code> on failure
     */
    public static boolean loadProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This connects to a remote <code>GreenTreeServer</code>, sends it an
     * {@link java.security.interfaces.RSAPublicKey} and {@link String} along
     * with instructions to register these on the server as a new {@link
     * com.greentree.model.domain.Token}.
     *
     * @param key <code>String</code> for the <code>Token</code>
     * @param ciphertext <code>RSAPublicKey</code> for the <code>Token</code>
     * @return <code>true</code>, if successful
     */
    public boolean registerToken(RSAPublicKey key, String ciphertext) {
        LOG.debug("boolean registerToken(RSAPublicKey, String) started");
        boolean result = false;
        try {
            out = this.getOut();
            
            result = out instanceof ObjectOutputStream;
            
            if (result) {
                LOG.debug("ObjectOutputStream loaded");
            } else {
                throw new IOException("ObjectOutputStream did not load");
            }
            
            out.writeObject("registerToken(RSAPublicKey, String)");
            out.writeObject(key);
            out.writeObject(ciphertext);
            in = this.getIn();
            result = (Boolean) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error("boolean registerToken(RSAPublicKey, String) threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }

        LOG.debug("registerToken(RSAPublicKey, String) returning "
            + String.valueOf(result));

        return result;
    }

    public boolean registerToken(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This connects to a remote <code>GreenTreeServer</code>, and sends it the
     * name of a service to initialize on the server.
     *
     * @param tokenService {@link String} names the service which this client
     * requires on the server.
     *
     * @return <code>true</code>, if the server reports success
     */
    public boolean registerService(String tokenService) {
        LOG.debug("boolean registerService(" + tokenService + ") started");
        boolean result = false;
        try {
            out = this.getOut();
            out.writeObject("registerService(String)");
            out.writeObject(tokenService);
            in = this.getIn();
            result = (Boolean) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error("boolean registerService(String tokenService) threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }

        LOG.debug("boolean registerService(" + tokenService + ") returned "
            + String.valueOf(result));

        return result;
    }

    public ArrayList<String> getData(RSAPublicKey key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public RSAPublicKey getPublicKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean addBlock(String msg, RSAPublicKey toKey, long time, long timeInMillis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
