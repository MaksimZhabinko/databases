select * from student;

EXPLAIN ANALYZE select * from student WHERE name = 'maksim';
CREATE INDEX student_index_name ON student (name);
DROP INDEX  IF EXISTS student_index_name;
EXPLAIN ANALYZE SELECT * FROM student WHERE name = 'maksim'; --b-tree

EXPLAIN ANALYZE select * from student WHERE phone_number = '+375291619626';
CREATE INDEX student_index_phone_number ON student USING hash (phone_number);
DROP INDEX  IF EXISTS student_index_phone_number;
EXPLAIN ANALYZE SELECT * FROM student WHERE phone_number = '+375291619626';--hash


EXPLAIN ANALYZE select * from student WHERE primary_skill = '@sql@';
create extension pg_trgm; 
DROP extension if exists pg_trgm;
CREATE INDEX student_index_primary_skill ON student USING gin (primary_skill gin_trgm_ops);
DROP INDEX  IF EXISTS student_index_primary_skill;
EXPLAIN ANALYZE SELECT * FROM student WHERE primary_skill = '@sql@';--GIN

EXPLAIN ANALYZE select * from student_address WHERE address = '@ovich';
create extension pg_trgm;
DROP extension if exists pg_trgm;
CREATE INDEX student_index_student_address ON student_address USING gist (address gist_trgm_ops);
DROP INDEX  IF EXISTS student_index_student_address;
EXPLAIN ANALYZE SELECT * FROM student_address WHERE address = '@ovich';--GIST

select * from subject;
select * from exam_result Order by student_id;
select * from student_address;

select * from student where name = 'maksim';

select * from student where surname LIKE '%bin%';

select * from student where phone_number LIKE '%61%';

select * from student s JOIN exam_result ex ON s.id=ex.student_id WHERE surname LIKE '%o%';

update student set name = '1123' where id=2;
select * from student  where id = 2;

INSERT INTO student (name, surname, date_of_birth, phone_number, primary_skill, created_datetime, updated_datetime) VALUES
('nfme$', 'surname', '1996-02-02', 'phone_number', 'primary_skill', '1996-02-02', '1996-02-02');

select * from student_subject_snapshot;

select average_mark_for_user(1);

update subject set name = '1123';
select average_mark_for_subject_name('1123');

select red_zone();

update student_address set id = 222 where id = 1;
update student_address set address = '222' where id = 1;
update student_address set student_id = 6 where id = 1;



