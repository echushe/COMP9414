% Program:  family.pl
% Source:   Prolog
%
% Purpose:  This is the sample program for the Prolog Lab in COMP9414/9814/3411.
%           It is a simple Prolog program to demonstrate how prolog works.
%           See lab.html for a full description.
%
% History:  Original code by Barry Drake


% parent(Parent, Child)
%
parent(albert, jim).
parent(albert, peter).
parent(jim, brian).
parent(john, darren).
parent(peter, lee).
parent(peter, sandra).
parent(peter, james).
parent(peter, kate).
parent(peter, kyle).
parent(brian, jenny).
parent(irene, jim).
parent(irene, peter).
parent(pat, brian).
parent(pat, darren).
parent(amanda, jenny).


% female(Person)
%
female(irene).
female(pat).
female(lee).
female(sandra).
female(jenny).
female(amanda).
female(kate).

% male(Person)
%
male(albert).
male(jim).
male(peter).
male(brian).
male(john).
male(darren).
male(james).
male(kyle).


% yearOfBirth(Person, Year).
%
yearOfBirth(irene, 1923).
yearOfBirth(pat, 1954).
yearOfBirth(lee, 1970).
yearOfBirth(sandra, 1973).
yearOfBirth(jenny, 2004).
yearOfBirth(amanda, 1979).
yearOfBirth(albert, 1926).
yearOfBirth(jim, 1949).
yearOfBirth(peter, 1945).
yearOfBirth(brian, 1974).
yearOfBirth(john, 1955).
yearOfBirth(darren, 1976).
yearOfBirth(james, 1969).
yearOfBirth(kate, 1975).
yearOfBirth(kyle, 1976).

% Rules of grandparents.
%
grandparent(Grandparent, Grandchild) :-
    parent(Grandparent, Child),
    parent(Child, Grandchild).


% Rules of couples.
%
couple(M, F) :-
    parent(M, Child),
    parent(F, Child),
    male(M),
    female(F).

% Rules of older.
%
older(Person1, Person2) :-
    yearOfBirth(Person1, Year1),
    yearOfBirth(Person2, Year2),
    Year2 > Year1.

% Rules of siblings.
%
siblings(Child1, Child2) :-   
    parent(Parent, Child1),
    parent(Parent, Child2),
    Child1 \= Child2.
 
% Rules of older brother.
%
olderBrother(Brother, Person) :-
    siblings(Brother, Person),
    older(Brother, Person),
    male(Brother).

% Recursive case 1:
%
descendant(Person, Descendant) :-
    parent(Person, Descendant).
descendant(Person, Descendant) :-
    parent(Person, Child),
    descendant(Child, Descendant).
    
% Recursive case 2:
%
ancestor(Person, Ancestor) :-
    parent(Ancestor, Person).
ancestor(Person, Ancestor) :-
    parent(Parent, Person),
    ancestor(Parent, Ancestor).

% Question 56
children(Parent, Children) :-
    findall(Child, parent(Parent, Child), Children).

% Question 57
%    
sibling_list(Child, Siblings) :-
    setof(Sibling, siblings(Child, Sibling), Siblings).
    
% Question 62
%
% list count
 listCount([], 0).

 listCount([_|List], Count2) :-
    listCount(List, Count1),
    Count2 is Count1 + 1.

% List of descendants of a person
%
descendant_list(Person, Descendants) :-
    findall(Descendant, descendant(Person, Descendant), Descendants).

% Count descendants of a person
%
countDescendants(Person, Count) :-
    descendant_list(Person, Descendants),
    listCount(Descendants, Count).


    
    
    
    
    
    
    
    
    