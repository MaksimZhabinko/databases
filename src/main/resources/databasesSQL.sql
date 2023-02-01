CREATE DATABASE university;

drop table if exists student CASCADE;
drop table if exists subject CASCADE;
drop table if exists exam_result;
drop table if exists student_address;
drop table if exists student_subject_snapshot;
DROP extension if exists pg_trgm;

CREATE TABLE student (
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL CHECK (name NOT LIKE '%@%' AND name NOT LIKE '%#%' AND name NOT LIKE '%$%'),
	surname VARCHAR(255) NOT NULL,
	date_of_birth DATE NOT NULL,
	phone_number VARCHAR(255) NOT NULL UNIQUE,
	primary_skill VARCHAR(255),
	created_datetime DATE NOT NULL,
	updated_datetime DATE NOT NULL
);

CREATE TABLE subject (
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	tutor VARCHAR(255) NOT NULL
);

CREATE TABLE exam_result (
	student_id BIGINT NOT NULL,
	subject_id BIGINT NOT NULL,
	mark BIGINT NOT NULL,
	CONSTRAINT fk_x_student FOREIGN KEY(student_id) REFERENCES student (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_x_subject FOREIGN KEY(subject_id) REFERENCES subject (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE student_address (
	id SERIAL PRIMARY KEY,
	address VARCHAR(255) NOT NULL,
	student_id BIGINT NOT NULL,
	CONSTRAINT fk_address_x_student FOREIGN KEY(student_id) REFERENCES student (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE OR REPLACE FUNCTION update_student_last_datetime() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_datetime=now();
    RETURN NEW;
END; $$
LANGUAGE plpgsql;

CREATE TRIGGER update_last_datetime
BEFORE UPDATE ON student
FOR EACH ROW EXECUTE PROCEDURE update_student_last_datetime();

CREATE OR REPLACE FUNCTION check_id_change()
  RETURNS TRIGGER AS
$BODY$
BEGIN
  IF NEW."id" IS DISTINCT FROM OLD."id"
  THEN
    RAISE EXCEPTION '"id" column cannot get updated';
  END IF;
  
  IF NEW."address" IS DISTINCT FROM OLD."address"
  THEN
    RAISE EXCEPTION '"address" column cannot get updated';
  END IF;
  
  IF NEW."student_id" IS DISTINCT FROM OLD."student_id"
  THEN
    RAISE EXCEPTION '"student_id" column cannot get updated';
  END IF;

  RETURN NEW;
END;
$BODY$ LANGUAGE PLPGSQL;

CREATE TRIGGER student_address_trigger 
AFTER UPDATE ON student_address FOR EACH ROW
      EXECUTE PROCEDURE check_id_change();

CREATE OR REPLACE FUNCTION base26_encode(IN digits bigint, IN min_width int = 0)
  RETURNS varchar AS $$
        DECLARE
          chars char[];
          ret varchar;
          val bigint;
      BEGIN
      chars = ARRAY['A','B','C','D','E','F','G','H','I','J','K','L','M'
                    ,'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
      val = digits;
      ret = '';
      IF val < 0 THEN
          val = val * -1;
      END IF;
      WHILE val != 0 LOOP
          ret = chars[(val % 26)+1] || ret;
          val = val / 26;
      END LOOP;

      IF min_width > 0 AND char_length(ret) < min_width THEN
          ret = lpad(ret, min_width, '0');
      END IF;

      RETURN ret;
 
END;
$$ LANGUAGE 'plpgsql' IMMUTABLE;

CREATE OR REPLACE FUNCTION average_mark_for_user(IN student_id_number bigint)
  RETURNS numeric AS $$
      BEGIN
	  RETURN (select AVG(mark) from student s join exam_result ex ON s.id=ex.student_id WHERE s.id = student_id_number); 
END;
$$ LANGUAGE 'plpgsql' IMMUTABLE;

CREATE OR REPLACE FUNCTION average_mark_for_subject_name(IN subject_name varchar)
  RETURNS numeric AS $$
      BEGIN
	  RETURN (select AVG(mark) from subject s join exam_result ex ON s.id=ex.student_id WHERE s.name = subject_name); 
END;
$$ LANGUAGE 'plpgsql' IMMUTABLE;

DROP FUNCTION if exists red_zone();
CREATE OR REPLACE FUNCTION red_zone()
  RETURNS TABLE (student_id_res BIGINT) AS $$
      BEGIN
	  RETURN query (select student_id from (select student_id, count(*) c from exam_result where mark <= 3 GROUP BY student_id) m WHERE m.c>=2);
END;
$$ LANGUAGE 'plpgsql' VOLATILE; 

INSERT INTO student (name, surname, date_of_birth, phone_number, primary_skill, created_datetime, updated_datetime) SELECT 
	initcap(base26_encode(substring(random()::text,3,10)::bigint)),
    initcap(base26_encode(substring(random()::text,3,10)::bigint)),
    LOCALTIMESTAMP - interval '50 years' * random(),
    '+' || trunc(random() * (999999999999-100000000000) + 100000000000),
	initcap(base26_encode(substring(random()::text,3,10)::bigint)),
	LOCALTIMESTAMP - interval '2 years' * random(),
	LOCALTIMESTAMP - interval '2 years' * random()
FROM generate_series(1, 100000) s(i);

INSERT INTO student (name, surname, date_of_birth, phone_number, primary_skill, created_datetime, updated_datetime) 
VALUES ('maksim', 'zhabinko', '1996-02-23', '+375291619626', 'java sql databases spring', '2000-01-01', '2023-01-01');

INSERT INTO subject (name, tutor) SELECT 
	initcap(base26_encode(substring(random()::text,3,10)::bigint)),
    initcap(base26_encode(substring(random()::text,3,10)::bigint))
FROM generate_series(1, 1000) s(i);

INSERT INTO exam_result (student_id, subject_id, mark) SELECT 
	trunc(random() * 100000 + 1),
    trunc(random()* 1000 + 1),
	trunc(random()* 10 + 1)
FROM generate_series(1, 1000000) s(i); 

INSERT INTO student_address (address, student_id) SELECT 
	initcap(base26_encode(substring(random()::text,3,10)::bigint)),
    trunc(random()* 10 + 1)
FROM generate_series(1, 10) s(i); 

INSERT INTO student_address (address, student_id) VALUES 
	('Sharangovicha 38', 1);


create table student_subject_snapshot as 
	select s.name as student_name, s.surname, su.name, e.mark 
	from student s, subject su, exam_result e
	where s.id = e.student_id and e.subject_id = su.id;











