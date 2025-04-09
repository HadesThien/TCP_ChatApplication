-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 04, 2025 at 05:18 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `chatserver`
--

-- --------------------------------------------------------

--
-- Table structure for table `message`
--

CREATE TABLE `message` (
  `Message_Id` int(11) NOT NULL,
  `Message_Text` varchar(500) NOT NULL,
  `Message_Date` date NOT NULL,
  `Sender` varchar(100) NOT NULL,
  `Receiver` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `message`
--

INSERT INTO `message` (`Message_Id`, `Message_Text`, `Message_Date`, `Sender`, `Receiver`) VALUES
(1, 'ádf', '2025-04-02', 'hadesthien', 'hadesthien'),
(2, 'hello', '2025-04-02', 'usera', 'usera'),
(3, 'Hello', '2025-04-02', 'hadesthien', 'usera'),
(4, 'user a ơi', '2025-04-04', 'hadesthien', 'usera'),
(5, 'sao á hadesthien', '2025-04-04', 'usera', 'hadesthien'),
(6, 'Xin chào mọi người', '2025-04-04', 'usera', 'usera');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `Username` varchar(100) NOT NULL,
  `Password` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`Username`, `Password`) VALUES
('hadesthien', '202cb962ac59075b964b07152d234b70'),
('usera', '697aa03927398125bb6282e2f414a6be');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `message`
--
ALTER TABLE `message`
  ADD PRIMARY KEY (`Message_Id`),
  ADD KEY `fk_sender` (`Sender`),
  ADD KEY `fk_receiver` (`Receiver`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`Username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `message`
--
ALTER TABLE `message`
  MODIFY `Message_Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `message`
--
ALTER TABLE `message`
  ADD CONSTRAINT `fk_receiver` FOREIGN KEY (`Receiver`) REFERENCES `user` (`Username`),
  ADD CONSTRAINT `fk_sender` FOREIGN KEY (`Sender`) REFERENCES `user` (`Username`),
  ADD CONSTRAINT `fk_username` FOREIGN KEY (`Sender`) REFERENCES `user` (`Username`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
