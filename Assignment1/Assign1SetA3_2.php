<?php
session_start();
$_SESSION['n']=$_POST['n'];
$_SESSION['c']=$_POST['c'];
$_SESSION['a']=$_POST['a'];
$_SESSION['start']=time();
?>
<!DOCTYPE html>
<html lang="en">
<body>
    <form action="Assign1SetA3_3.php" method="post">
        <pre>
            Enter the Subject Marks(Sep by ,) : <input type="text" name="s1">
            
            <input type="submit">      <input type="reset" value="clear">
        </pre>
    </form>
</body>
</html>