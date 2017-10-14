# mvn clean compile package install

# java -jar "target/parser.jar" --duration=hourly --startDate="2017-01-01.13:00:00" --threshold=100  --accesslog=/home/ndennis/Downloads/access.log




mysql> select a.ip from access_log a where a.access_date>='2017-01-01 13:00:00.0' and a.access_date<'2017-01-01 14:00:00.0' group by a.ip having count(distinct a.id)>100



CREATE TABLE access_log_filtered_copy SELECT b.* FROM access_log b inner join (select a.ip from access_log a WHERE a.access_date >= '2017-01-01 13:00:00.0' and a.access_date < '2017-01-01 14:00:00.0' GROUP BY a.ip HAVING COUNT(DISTINCT a.id)> 100 ) c on b.ip=c.ip where b.access_date >='2017-01-01 13:00:00.0' and b.access_date < '2017-01-01 14:00:00.0'