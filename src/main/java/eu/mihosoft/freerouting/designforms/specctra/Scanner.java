/*
 *   Copyright (C) 2014  Alfons Wirtz
 *   website www.freerouting.net
 *
 *   Copyright (C) 2017 Michael Hoffer <info@michaelhoffer.de>
 *   Website www.freerouting.mihosoft.eu
*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * Scanner.java
 *
 * Created on 4. Juli 2004, 19:13
 */

package eu.mihosoft.freerouting.designforms.specctra;

/**
 * Interface for scanner generated by jflex.
 *
 * @author Alfons Wirtz
 */
public interface Scanner
{
    /**
     * Reads the next token from the input file.
     */
    Object nextToken() throws java.io.IOException;
    
    /**
     * Starts a new state.
     */
    void yybegin(int p_new_state);
}
