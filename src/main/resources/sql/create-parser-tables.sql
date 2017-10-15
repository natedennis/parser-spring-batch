CREATE TABLE `access_log` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `access_date` datetime NOT NULL,
  `ip` varchar(15) NOT NULL,
  `request` varchar(2050) NOT NULL,
  `status` int(10) NOT NULL,
  `user_agent` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ip` (`ip`),
  KEY `access_date` (`access_date`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
