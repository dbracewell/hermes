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
ALPHANUM = {ALPHA}"'"?({ALPHA}|[:digit:])*

UNDERSCORE={ALPHANUM}("_"+){ALPHANUM}

//Common English contractions
CONTRACTION=({APOS}[sSmMdD]|{APOS}ll|{APOS}re|{APOS}ve|{APOS}LL|{APOS}RE|{APOS}VE|[sS]{APOS}|[nN]{APOS}[tT])

PERSON_TITLE= ("gen"|"mr"|"ms"|"miss"|"Master"|"Rev"|"Fr"|"Dr"|"Atty"|"Prof"|"Hon"|"Pres"|"Gov"|"Coach"|"Ofc"|"ms"|"miss"|"mrs"|"mr"|"master"|"rev"|"fr"|"dr"|"atty"|"prof"|"hon"|"pres"|"gov"|"coach"|"ofc"|"MS"|"MISS"|"MRS"|"MR"|"MASTER"|"REV"|"FR"|"DR"|"ATTY"|"PROF"|"HON"|"PRES"|"GOV"|"COACH"|"OFC"|"ph.d"|"Ph.d"|"Ph.D"|"PH.D"|"Phd")"."?

ACRONYM =[A-Z]("."{ALPHANUM}+)+"."?

ABBREVIATION = [A-Z]"."

COMPANY = {ALPHANUM} ("&"|"@") {ALPHANUM}

NUMBER=[:digit:]+([\.,][:digit:]+)*("st"|"th"|"rd")?

HASHTAG="#" {ALPHA}({ALPHA}|[:digit:])*

REPLY = "@" [a-zA-Z0-9_]{1,15}

TIME = [:digit:]+ ":" [:digit:]+ {AMPM}?

AMPM = ([AaPp]"."?[mM]"."?)

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
SEGMENT=({UNRESERVED}|[:digit:]|{PCT_ENCODED}|{SUBDELIMS}|":"|"@")*
UNRESERVED=({ALPHANUM}|"-"|"."|"_"|"~")
SUBDELIMS=("!"|"$"|"&"|"'"|"("|")"|"*"|"+"|","|";"|"=")
PCT_ENCODED="%"{HEXDIG}{HEXDIG}
HEXDIG=([:digit:]|"A"|"B"|"C"|"D"|"E"|"F"|"a"|"b"|"c"|"d"|"e"|"f")

//===================================================================================================================
// Misc
//===================================================================================================================

CURRENCY = [$\u00A2\u00A3\u00A5\u20A0-\u20CF]

WHITESPACE = [\p{Z}\t\f\r\n\p{C}]
MATH=[\u2200-\u22ff]
EMOTICON=[\u219d\u2300-\u2800\ud800-\uddff\ude00-\ue079\ue200-\ue263\ue3ff-\ue466\ue503-\uefff\uf03d-\uf296\ufe00-\ufe0f]+

//EMOTICON = [\uD83C\uDC3E\uDC4D\uDC4F\uDC53\uDC66\uDC6C\uDC83\uDC8B\uDC8C\uDC8D\uDC95\uDC96\uDC98\uDC99\uDC9B\uDC9D\uDCAA\uDD10\uDD25\uDDF8\uDDFA\uDE00\uDE0A\uDE0D\uDE0E\uDE18\uDE22\uDE2E\uDE30\uDE4B\uDEBC\uDF7C\uDFB6\uDFFB\uDFFC\ud83d\ude03\u1F567-\u1F900\u0023\u002A\u0030-\u0039\u00A9\u00AE\u1F004\u1F0CF\u1F170-\u1F171\u1F17E-\u1F17F\u1F18E\u1F191-\u1F19A\u1F1E6-\u1F202\u1F21A\u1F22F\u1F232-\u1F23A\u1F250-\u1F251\u1F300-\u1F6F6\u1F910-\u1F95E\u1F980-\u1F991\u1F9C0\u200D\u203C\u2049\u20E3\u2122\u2139\u2194-\u21AA\u231A-\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA\u24C2\u25AA-\u25AB\u25B6\u25C0\u25FB\u25FC\u25FD\u25FE\u2600\u2601\u2602\u2603\u2604\u260E\u2611\u2614\u2615\u2618\u261D\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638\u2639\u263A\u2640\u2642\u2648\u2649\u264A\u264B\u264C\u264D\u264E\u264F\u2650\u2651\u2652\u2653\u2660\u2663\u2665\u2666\u2668\u267B\u267F\u2692\u2693\u2694\u2695\u2696\u2697\u2699\u269B\u269C\u26A0\u26A1\u26AA\u26AB\u26B0\u26B1\u26BD\u26BE\u26C4\u26C5\u26C8\u26CE\u26CF\u26D1\u26D3\u26D4\u26E9\u26EA\u26F0\u26F1\u26F2\u26F3\u26F4\u26F5\u26F7\u26F8\u26F9-\u27BF\u2764\u2934-\u2935\u2B05-\u2B07\u2B1B-\u2B1C\u2B50\u2B55\u3030\u303D\u3297\u3299\uFE0F]+


%%
<YYINITIAL>{
 {HASHTAG}              {return attachToken(TokenType.HASH_TAG);}
 {REPLY}                {return attachToken(TokenType.REPLY);}
 {TIME}                {return attachToken(TokenType.TIME);}
 {AMPM}                {return attachToken(TokenType.TIME);}
 {NUMBER}               {return attachToken(TokenType.NUMBER);}
 {ALPHANUM}({HYPHEN}{ALPHANUM})+ {return attachToken(TokenType.ALPHA_NUMERIC);}
 {HYPHEN}               {return attachToken(TokenType.HYPHEN);}
 {CURRENCY}             {return attachToken(TokenType.MONEY);}
 {CONTRACTION}          {return attachToken(TokenType.CONTRACTION);}
 {ABBREVIATION}         {return attachToken(TokenType.ACRONYM);}
 {ALPHANUM}/{CONTRACTION} {return attachToken(TokenType.ALPHA_NUMERIC);}
 {PCT_ENCODED}          {return attachToken(TokenType.NUMBER);}
 {ALPHANUM}             {return attachToken(TokenType.ALPHA_NUMERIC);}
 {PUNCTUATION}          {return attachToken(TokenType.PUNCTUATION);}
 {CJ}                   {return attachToken(TokenType.CHINESE_JAPANESE);}
 {EMAIL}                {return attachToken(TokenType.EMAIL);}
 {PERSON_TITLE}         {return attachToken(TokenType.PERSON_TITLE);}
 {ALPHANUM}/{PUNCTUATION}{PERSON_TITLE} {return attachToken(TokenType.ALPHA_NUMERIC);}
 {URI}/{WHITESPACE}|{PUNCTUATION}  {return attachToken(TokenType.URL);}
 {ACRONYM}              {return attachToken(TokenType.ACRONYM);}
 {COMPANY}              {return attachToken(TokenType.COMPANY);}
 {UNDERSCORE}           {return attachToken(TokenType.ALPHA_NUMERIC);}
 {URI}                  {return attachToken(TokenType.URL);}
 {EMOTICON}              {return attachToken(TokenType.EMOTICON);}
 {MATH}    {return attachToken(TokenType.UNKNOWN);}
 {WHITESPACE}           {}
}

[^]                   {return attachToken(TokenType.UNKNOWN);}
