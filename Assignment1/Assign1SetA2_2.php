<?php
setcookie('ffont',$_POST['font']);
setcookie('fsize',$_POST['size']);
setcookie('fcolor',$_POST['color']);
setcookie('fbgcolor',$_POST['bgcolor']);

echo "<br> Font font : ".$_POST['font']."<br>";
echo "<br> Font size : ".$_POST['size']."<br>";
echo "<br> Font color : ".$_POST['color']."<br>";
echo "<br> Background color : ".$_POST['bgcolor']."<br>";
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <form action="Assign1SetA2_3.php" method="post">
        <pre>
            Enter the Text To Modify : <input type="text" name="t">
            <input type="submit">  <input type="reset">
        </pre>
    </form>
    
</body>
</html>