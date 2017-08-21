% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%     COMP9414 Assignment 1
%       
%       Programmed by:
%           Chunnan Sheng
% 
%       Student Code:
%           z5100764
%
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%       Question 1
%
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Base predicate
% Result of empty list should be zero.
%
weird_sum([], 0).

% Recursive predicate for new head larger than or equal to 5
%
weird_sum([Head|List], NewResult) :-
    Head >= 5,
    weird_sum(List, OldResult),
    NewResult is OldResult + Head * Head.

% Recursive predicate for new head less than or equal to 2
%    
weird_sum([Head|List], NewResult) :-
    Head =< 2,
    weird_sum(List, OldResult),
    NewResult is OldResult - abs(Head).

% Recursive predicate for new head larger than 2 but less than 5
% The result of calculation will stay unchanged.
%    
weird_sum([Head|List], NewResult) :-
    Head > 2,
    Head < 5,
    weird_sum(List, NewResult).

    
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%       Question 2
%
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Relationship of father and son (or daughter)
%
father(Father, Child) :-
    parent(Father, Child),
    male(Father).

% Start point of paternal relationship:
% father and son (or daughter)
%
paternity(Person1, Person2) :-
    father(Person1, Person2).

% Recursion of paternal relationship
%
paternity(Person1, Person3) :-
    father(Person1, Person2),
    paternity(Person2, Person3).
    
% Case 1:
% The same person exactly has the same family name.
%
same_name(Person, Person).

% Case 2: 
% They share the same family name if 
% they are father and son (or daughter). 
%
same_name(Person1, Person2) :-
    father(Person1, Person2).
same_name(Person1, Person2) :-
    father(Person2, Person1).
    
% Case 3:
% They share the same family name if
% they share one paternal ancestor.
%
same_name(Person1, Person2) :-
    not(father(Person1, Person2)),
    not(father(Person2, Person1)),
    paternity(Ancestor, Person1),
    paternity(Ancestor, Person2),
    Person1 \= Person2.

% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%       Question 3
%
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Start point
% Empty list would generate empty result list.
%
log_table([], []).

% Recursive logic of calculation
% New number in the first list should be larger than zero.
%
log_table([Head1|List1], [Head2|List2]) :-
    log_table(List1, List2),
    Head2 = [Head1, Result],
    Head1 > 0.0,
    Result is log(Head1).

% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%       Question 4
%
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Even or odd
%
even(Number) :-
    integer(Number),
    0 is Number mod 2.
    
odd(Number) :-
    integer(Number),
    1 is Number mod 2.
    
% Empty List should be accepted
%
paruns([], [[]]).

% List with one element as a start point
%
paruns([Element], [[Element]]).

% Recursive function of paruns:
% Add a sub head to the head of the new list if
% the new element has the same odd or even property.
%
paruns([Head_a|List_a], [NewHead2|NewList]) :-
    List_a = [Head_b|_],
    (
        (odd(Head_a),odd(Head_b));
        (even(Head_a),even(Head_b))
    ),
    NewHead2 = [Head_a|NewHead1],
    paruns(List_a, [NewHead1|NewList]).
    
% Recursive function of paruns:
% Add a new head to new list if
% the new element has a different odd or even property.
%
paruns([Head_a|List_a], [NewHead|NewList]) :-
    List_a = [Head_b|_],
    (
        (odd(Head_a),even(Head_b));
        (even(Head_a),odd(Head_b))
    ),
    NewHead = [Head_a],
    paruns(List_a, NewList).


% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%       Question 5
%
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Start point:
% Both left and right nodes are empty
%
is_heap(tree(empty, Number, empty)) :-
    integer(Number).

% Recursive function with only right node empty:
% Left sub tree should be a heap then the entire tree is a heap.
%    
is_heap(tree(SubTreeA, Number, empty)) :-
    SubTreeA = tree(_, NumberA, _),
    Number =< NumberA,
    is_heap(SubTreeA).

% Recursive function with only left node empty:
% Right sub tree should be a heap then the entire tree is a heap.
%     
is_heap(tree(empty, Number, SubTreeB)) :-
    SubTreeB = tree(_, NumberB, _),
    Number =< NumberB,
    is_heap(SubTreeB).

% Recursive function with both left and right nodes are sub trees:
% Both sub trees should be heaps.
%      
is_heap(tree(SubTreeA, Number, SubTreeB)) :-
    SubTreeA = tree(_, NumberA, _),
    SubTreeB = tree(_, NumberB, _),
    Number =< NumberA,
    Number =< NumberB,
    is_heap(SubTreeA),
    is_heap(SubTreeB).
    









