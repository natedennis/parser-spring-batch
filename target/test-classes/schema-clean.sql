set foreign_key_checks = 0;

delete from batch_step_execution_context;
delete from batch_job_execution_context;
delete from batch_step_execution;
delete from batch_job_execution;
delete from batch_job_execution_params;
delete from batch_job_instance;

delete from batch_step_execution_seq;
delete from batch_job_execution_seq;
delete from batch_job_seq;

insert into batch_step_execution_seq values(0, '0');
insert into batch_job_execution_seq values(0, '0');
insert into batch_job_seq values(0, '0');

set foreign_key_checks = 1;