<!-- 
Write a PHP program to accept student name, class, address on the first form. After submitting
the first form, accept the marks of 6 subject on the next form. After the second form is submitted,
display the marklist of student on third form.
 -->
<?php
    session_start();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <form action="Assign1SetA3_2.php" method="post">
        <pre>
            Enter the Name : <input type="text" name="n">
            Enter the Class : <input type="text" name="c">
            Enter the Address : <input type="text" name="a">
            <input type="submit">      <input type="reset" value="clear">
        </pre>
    </form>
</body>
</html>