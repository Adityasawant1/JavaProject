<?php
session_start();
$_SESSION['un']=$_POST['un'];
$_SESSION['pwd']=$_POST['pwd'];
$_SESSION['start']=time();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <form action="Assign1SetA1_3.php" method="post">
        <pre>
            Enter the Name : <input type="text" name="n">
            Enter the City : <input type="text" name="c">
            Enter the Phone : <input type="text" name="p">
            <input type="submit">      <input type="reset" value="clear">
        </pre>
    </form>
</body>
</html>