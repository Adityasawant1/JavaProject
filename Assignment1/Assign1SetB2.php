<!-- 
Write a PHP program to accept the different hobbies from the user. After the user has selected the
hobbies, display the selected options on the same form. Also keep the selected options “checked”
on the same form. 
-->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sticky Select</title>
</head>
<body>
    <form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post">
        <pre>
            Choose Hobbies :
            <select name="font[]" multiple>
                <option value="Reading" <?php if (isset($_POST['font']) && in_array("Reading", $_POST['font'])) echo "selected"; ?>>Reading</option>
                <option value="Gaming" <?php if (isset($_POST['font']) && in_array("Gaming", $_POST['font'])) echo "selected"; ?>>Gaming</option>
                <option value="Music" <?php if (isset($_POST['font']) && in_array("Music", $_POST['font'])) echo "selected"; ?>>Music</option>
            </select>
            <input type="submit">   <input type="reset">
        </pre>
    </form>
<?php
if (isset($_POST['font'])) {
    $ar = $_POST['font'];
    $h = "";
    foreach ($ar as $x) {
        $h .= $x . " ";
    }
    echo "You Selected Hobbies: " . trim($h);
} else {
    echo "You did not select any hobbies.";
}
?>
</body>
</html>