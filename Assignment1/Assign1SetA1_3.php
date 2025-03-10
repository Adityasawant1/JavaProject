<?php
session_start();
$_SESSION['current']=time();
$_SESSION['n']=$_POST['n'];
$_SESSION['c']=$_POST['c'];
$_SESSION['p']=$_POST['p'];

if($_SESSION['current']-$_SESSION['start']>60)
{
    echo "Session Timed Out";
}
else{
    echo "Username : ".$_SESSION['un']."<br>";
    echo "Password : ".$_SESSION['pwd']."<br>";
    echo "Name : ".$_SESSION['n']."<br>";
    echo "City : ".$_SESSION['c']."<br>";
    echo "Phone : ".$_SESSION['p']."<br>";
}
session_unset();
session_destroy();

?>