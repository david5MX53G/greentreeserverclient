/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.greentree.controller;

import com.greentree.model.domain.Token;

/**
 *
 * @author david5MX53G
 */
public interface IInterceptingController {
    public boolean performAction(String commandString, Token token);
}
