<!-- 
Write a PHP program to display the different hobbies to user. After the user has selected hobbies,
display the user selected hobbies on the same form (use select and option tag). 
-->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post">
        <pre>
            Choose Hobbies :
            <select name="hobbies[]" multiple>
                <option value="Reading">Reading</option>
                <option value="Gaming">Gaming</option>
                <option value="Music">Music</option>
            </select>
            <input type="submit">   <input type="reset">
        </pre>
    </form>
<?php
if (isset($_POST['hobbies'])) {
    $ar = $_POST['hobbies'];
    $h = "";
    foreach ($ar as $x) {
        $h .= "$x ";
    }
    echo "You Selected Hobbies: " . trim($h);
} else {
    echo "You did not select any hobbies.";
}
?>
</body>
</html>
