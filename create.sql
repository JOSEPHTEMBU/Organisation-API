CREATE DATABASE mulamaapi;

\c mulamaapi

CREATE TABLE news(
    id SERIAL PRIMARY KEY,
    title VARCHAR,
    content VARCHAR,
    sectionId INTEGER
);

CREATE TABLE users(
    id SERIAL PRIMARY KEY,
    name VARCHAR,
    userPosition VARCHAR,
    role VARCHAR,
    sectionId INTEGER
);

CREATE TABLE sections(
    id SERIAL PRIMARY KEY,
    sectionName VARCHAR,
    description VARCHAR
);