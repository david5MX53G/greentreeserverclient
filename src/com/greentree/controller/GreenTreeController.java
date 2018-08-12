/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.greentree.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.logging.Level;
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
    
    /** This is used to identify the method in log messages */
    private String methodName;

    /**
     * This instantiates the {@link GreenTreeController#instance_} of this
     * class.
     */
    private GreenTreeController() {
        try {
            inetAddr = InetAddress.getLocalHost();
        } catch (IOException ex) {
            LOG.error("constructor threw " + ex.getClass().getSimpleName()
                + ": " + ex.getMessage());
        }
        LOG.debug("GreenTreeController() complete");
    }
    
    /**
     * This ensures the {@link GreenTreeController#socket} and associated 
     * streams are closed.
     * 
     * @throws Throwable when badness happens
     */
    @Override
    protected void finalize() throws Throwable {
        this.closeSocket();
        super.finalize();
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
     * This opens a {@link GreenTreeController#socket}, {@link 
     * GreenTreeController#in}, and {@link GreenTreeController#out}.
     */
    private boolean openSocket() {
        boolean result = false;
        try {
            methodName = "boolean openSocket()";
            
            // open a socket
            socket = new Socket(inetAddr, PORT);
            if (socket instanceof Socket) {
                LOG.debug(methodName + " Socket acquired");
            } else {
                throw new IOException(methodName + " Socket failed");
            }
            
            // open outputstream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            if (out instanceof ObjectOutputStream) {
                LOG.debug(methodName + " ObjectOutputStream acquired");
            } else {
                throw new IOException(this.getClass().getSimpleName()
                    + " ObjectOutputStream getOut() failed");
            }
            
            // open inputstream
            in = new ObjectInputStream(socket.getInputStream());
            if (in instanceof ObjectInputStream) {
                LOG.debug(methodName + " ObjectInputStream acquired");
            } else {
                throw new IOException(this.getClass().getSimpleName()
                    + " ObjectOutputStream getOut() failed");
            }
            
            // update result
            result = true;
        } catch (IOException e) {
            LOG.error(methodName + " threw " + e.getClass().getSimpleName() 
                + ": " + e.getMessage());
        }
        return result;
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
            methodName = "closeSocket";
            if (in != null) {
                in.close();
            }
            
            if (out != null) {
                out.close();
            }
            
            if (socket != null) {
                socket.close();
            }
            
            result = true;
            LOG.debug(methodName + " complete");
        } catch (NullPointerException | IOException ex) {
            result = false;
            LOG.error(methodName + " threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }

    /**
     * This connects to a remote <code>GreenTreeServer</code>, sends it an
     * {@link java.security.interfaces.RSAPublicKey} and {@link String} with 
     * which to authenticate a {@link com.greentree.model.domain.Token}.
     *
     * @param key <code>String</code> for the <code>Token</code>
     * @param ciphertext <code>RSAPublicKey</code> for the <code>Token</code>
     * @return <code>true</code>, if successful
     */
    public boolean registerToken(RSAPublicKey key, String ciphertext) {
        methodName = "boolean registerToken(RSAPublicKey, String)";
        LOG.debug(methodName + " started");
        boolean result = false;
        try {
            this.openSocket();
            String command = "registerToken(RSAPublicKey, String)";
            
            out.writeObject(command);
            LOG.debug(methodName + " command String written to socket");
            
            out.writeObject(key);
            LOG.debug(methodName + " RSAPublicKey written to socket");
            
            out.writeObject(ciphertext);
            LOG.debug(methodName + " ciphertext String written to socket");
                        
            result = (Boolean) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error(methodName + " threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }

        LOG.debug(methodName + " returning " + String.valueOf(result));
        return result;
    }

    /**
     * This connects to a remote <code>GreenTreeServer</code> and registers a 
     * new {@link com.greentree.model.domain.Token}.
     * 
     * @param plaintext {@link String} will be used to authenticate 
     * @return <code>true</code>, if successful
     */
    public boolean registerToken(String plaintext) {
        methodName = "boolean registerToken(String)";
        LOG.debug(methodName + "started");
        boolean result = false;
        try {
            this.openSocket();
            String command = "registerToken(String)";
            
            out.writeObject(command);
            LOG.debug(methodName + " command String written to socket: " 
                + command);
            
            out.writeObject(plaintext);
            LOG.debug(methodName + " plaintext written to socket");
            
            result = (Boolean) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error(methodName + "threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }

        LOG.debug(methodName + " result: " + String.valueOf(result));
        return result;
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
        methodName = "boolean registerService(String)";
        LOG.debug(methodName + " started: " + tokenService);
        boolean result = false;
        
        try {
            this.openSocket();
            out.writeObject("registerService(String)");
            out.writeObject(tokenService);
            result = (Boolean) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error("boolean registerService(String tokenService) threw "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }

        LOG.debug(methodName + " returned " + String.valueOf(result));
        return result;
    }

    public ArrayList<String> getData(RSAPublicKey key) {
        methodName = "ArrayList<String> getData(RSAPublicKey)";
        ArrayList<String> result = null;
        
        try {
            this.openSocket();
            out.writeObject("getData(RSAPublicKey)");
            out.writeObject(key);
            result = (ArrayList<String>) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error(methodName + " threw " + ex.getClass().getSimpleName() 
                + " " + ex.getMessage());
        } finally {
            this.closeSocket();
        }
        
        return result;
    }

    /**
     * This connects to a remote <code>GreenTreeServer</code> and retrieves the 
     * {@link RSAPublicKey} of the active {@link Token} for this client on the 
     * server.
     * 
     * @return <code>RSAPublicKey</code> of the active client <code>Token</code>
     */
    public RSAPublicKey getPublicKey() {
        methodName = "RSAPublicKey getPublicKey()";
        LOG.debug(methodName + " started");
        RSAPublicKey result = null;
        
        try {
            this.openSocket();
            out.writeObject("getPublicKey()");            
            result = (RSAPublicKey) in.readObject();
            
            if (result instanceof RSAPublicKey) {
                LOG.debug(methodName + " acquired RSAPublicKey");
            } else {
                throw new ClassNotFoundException(" failed to get RSAPublicKey");
            }
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error(methodName + " threw " + ex.getClass().getSimpleName() 
                + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }
        
        return result;
    }

    public boolean addBlock(String msg, RSAPublicKey toKey, long notBefore, long notAfter) {
        methodName = "addBlock(String, RSAPublicKey, long, long)";
        LOG.debug(methodName + " started");
        boolean result = false;
        
        try {
            this.openSocket();
            out.writeObject("addBlock(String, RSAPublicKey, long, long)");
            out.writeObject(msg);
            out.writeObject(toKey);
            out.writeObject(notBefore);
            out.writeObject(notAfter);
            result = (boolean) in.readObject();
            
            if (result) {
                LOG.debug(methodName + " returned true");
            } else {
                throw new IOException(methodName + " returned false");
            }
        } catch (ClassNotFoundException | IOException ex) {
            LOG.error(methodName + " threw " + ex.getClass().getSimpleName() 
                + ": " + ex.getMessage());
        } finally {
            this.closeSocket();
        }
        
        return result;
    }
}
