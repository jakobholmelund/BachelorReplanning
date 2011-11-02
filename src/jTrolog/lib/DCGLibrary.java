/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jTrolog.lib;

/**
 * Library for managing DCGs.
 * Library/Theory dependency: BasicLibrary
 */
public class DCGLibrary extends Library {

	public DCGLibrary(){
	}

	public String getTheory(){
		return
		":- op(1200, xfx, '-->').  " +
		":- op(200, xfx, '\\').  " +

		"phrase(Category,String,Left, SyntaxTree) :- dcg_parse(Category,String\\Left, SyntaxTree). "+
		"phrase(Category,[H|T], SyntaxTree) :- dcg_parse(Category,[H|T]\\[], SyntaxTree). "+

        "dcg_parse(A,Tokens,  A < SubTree) :- " +
                "not list(A), " +
                "(A --> B), " +
                "dcg_parse(B,Tokens, SubTree). "+
		"dcg_parse((A,B),(Tokens \\ Xs), [SubTreeA|SubTreeB]) :- " +
                "dcg_parse(A,(Tokens \\ Tokens1), SubTreeA), " +
                "dcg_parse(B,(Tokens1 \\ Xs), SubTreeB). " +
		"dcg_parse(A, Tokens, A) :- " +
                "list(A), " +
                "dcg_connect(A,Tokens). "+
		"dcg_parse({A},(Xs \\ Xs), []) :- A. "+

        "dcg_connect([],(Xs \\ Xs)). "+
		"dcg_connect([W|Ws],([W|Xs] \\ Ys)) :- dcg_connect(Ws,(Xs \\ Ys)). ";
	}
}