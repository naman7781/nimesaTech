-- src/main/resources/schema.sql
CREATE TABLE job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service VARCHAR(255),
    status VARCHAR(50)
);

CREATE TABLE resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT,
    service VARCHAR(255),
    identifier VARCHAR(255),
    FOREIGN KEY (job_id) REFERENCES job(id)
);
