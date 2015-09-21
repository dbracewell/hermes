/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.hermes.tokenization.TokenType;

%%

%class StandardTokenizer
%public
%unicode
%type com.davidbracewell.hermes.tokenization.Tokenizer.Token
%function next
%pack
%caseless
%char
%{

private int index;

private final int yychar(){
    return yychar;
}

private Tokenizer.Token attachToken(TokenType type){
  Tokenizer.Token token=new Tokenizer.Token( yytext() , type, yychar(), yychar()+yylength(), index);
  index++;
  return token;
}


%}

//===================================================================================================================
// Punctuation
//===================================================================================================================
HYPHEN= \p{Pd}
APOS=['\0092\u2019\u0091\u2018\u201B]|&apos;
PUNCTUATION=!(![\p{P}]|{HYPHEN})

//===================================================================================================================
// Alpha Numeric
//===================================================================================================================

//Chinese or Japanese Characters
CJ=[\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

//Non-Chinese or Japanese Letter
ALPHA=!(![:letter:]|{CJ})

//Alphanumeric Character
ALPHANUM = ({ALPHA}|[:digit:])+

UNDERSCORE={ALPHANUM}("_"+){ALPHANUM}

//Common English contractions
CONTRACTION=({APOS}[sSmMdD]|{APOS}ll|{APOS}re|{APOS}ve|{APOS}LL|{APOS}RE|{APOS}VE|[sS]{APOS}|[nN]{APOS}[tT])

PERSON_TITLE= ("gen"|"mr"|"ms"|"miss"|"Master"|"Rev"|"Fr"|"Dr"|"Atty"|"Prof"|"Hon"|"Pres"|"Gov"|"Coach"|"Ofc"|"ms"|"miss"|"mrs"|"mr"|"master"|"rev"|"fr"|"dr"|"atty"|"prof"|"hon"|"pres"|"gov"|"coach"|"ofc"|"MS"|"MISS"|"MRS"|"MR"|"MASTER"|"REV"|"FR"|"DR"|"ATTY"|"PROF"|"HON"|"PRES"|"GOV"|"COACH"|"OFC"|"ph.d"|"Ph.d"|"Ph.D"|"PH.D"|"Phd")"."?

ACRONYM =[A-Z]("."{ALPHANUM}+)+"."?

ABBREVIATION = [A-Z]"."

COMPANY = {ALPHANUM} ("&"|"@") {ALPHANUM}

NUMBER=[:digit:]+([\.,][:digit:]+)*("st"|"th"|"rd")?


//===================================================================================================================
// Internet Related
//===================================================================================================================

//Email addresses
EMAIL={ALPHANUM}(("."|"-"|"_"){ALPHANUM})*"@"{ALPHANUM}(("."|"-"){ALPHANUM})+

// Absolute URI (Partial BNF from RFC3986) https://github.com/rdelbru/lucene-uri-preserving-standard-tokenizer
URI=({ALPHA}+"://"?({USERINFO}"@")?)?{AUTHORITY}{PATH}("?"{QUERY})?("#"{FRAGMENT})?
AUTHORITY={HOST}(":"{PORT})?
QUERY=({SEGMENT}|"/"|"?")*
FRAGMENT=({SEGMENT}|"/"|"?")*
USERINFO={USERNAME}(":"{PASSWD})?
USERNAME={UNRESERVED}+
PASSWD=({UNRESERVED}|":"|{SUBDELIMS})+
HOST={DOMAINLABEL}("."{DOMAINLABEL})*"."{TLD}
TLD = [a-zA-z]{2,4}
DOMAINLABEL={ALPHANUM}(("-"|{ALPHANUM})*{ALPHANUM})?
PORT=[:digit:]+
PATH=("/"{SEGMENT})*
SEGMENT=({UNRESERVED}|{PCT_ENCODED}|{SUBDELIMS}|":"|"@")*
UNRESERVED=({ALPHANUM}|"-"|"."|"_"|"~")
SUBDELIMS=("!"|"$"|"&"|"'"|"("|")"|"*"|"+"|","|";"|"=")
PCT_ENCODED="%"{HEXDIG}{HEXDIG}
HEXDIG=([:digit:]|"A"|"B"|"C"|"D"|"E"|"F"|"a"|"b"|"c"|"d"|"e"|"f")

SGML = "<" [^>]+ ">"


//===================================================================================================================
// Misc
//===================================================================================================================

CURRENCY = [$\u00A2\u00A3\u00A5\u20A0-\u20CF]

WHITESPACE = [\p{Z}\r\n\p{C}]


%%
<YYINITIAL>{
 {ALPHANUM}({HYPHEN}{ALPHANUM})+ {return attachToken(TokenType.ALPHA_NUMERIC);}
 {HYPHEN}               {return attachToken(TokenType.HYPHEN);}
 {CURRENCY}             {return attachToken(TokenType.MONEY);}
 {CONTRACTION}          {return attachToken(TokenType.CONTRACTION);}
 {ABBREVIATION}         {return attachToken(TokenType.ACRONYM);}
 {ALPHANUM}/{CONTRACTION} {return attachToken(TokenType.ALPHA_NUMERIC);}
 {PCT_ENCODED}          {return attachToken(TokenType.NUMBER);}
 {NUMBER}               {return attachToken(TokenType.NUMBER);}
 {ALPHANUM}             {return attachToken(TokenType.ALPHA_NUMERIC);}
 {PUNCTUATION}          {return attachToken(TokenType.PUNCTUATION);}
 {CJ}                   {return attachToken(TokenType.CHINESE_JAPANESE);}
 {EMAIL}                {return attachToken(TokenType.EMAIL);}
 {PERSON_TITLE}         {return attachToken(TokenType.PERSON_TITLE);}
 {ALPHANUM}/{PUNCTUATION}{PERSON_TITLE} {return attachToken(TokenType.ALPHA_NUMERIC);}
 {URI}/{WHITESPACE}|{PUNCTUATION}  {return attachToken(TokenType.URL);}
 {ACRONYM}              {return attachToken(TokenType.ACRONYM);}
 {SGML}                 {return attachToken(TokenType.SGML);}
 {COMPANY}              {return attachToken(TokenType.COMPANY);}
 {UNDERSCORE}           {return attachToken(TokenType.ALPHA_NUMERIC);}
 {URI}                  {return attachToken(TokenType.URL);}
 {WHITESPACE}           {}
}

  [^]                   {return attachToken(TokenType.UNKNOWN);}
