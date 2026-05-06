document.addEventListener('DOMContentLoaded', () => {
    const textarea = document.getElementById('textarea');
    const lineNumbersEle = document.getElementById('line-numbers');

    const textareaStyles = window.getComputedStyle(textarea);
    [
        'fontFamily',
        'fontSize',
        'fontWeight',
        'letterSpacing',
        'lineHeight',
        'padding'
    ].forEach((property) => {
        lineNumbersEle.style[property] = textareaStyles[property];
    });

    const parseValue = (v) =>
        v.endsWith('px') ? parseInt(v.slice(0, -2), 10) : 0;

    const font = `${textareaStyles.fontSize} ${textareaStyles.fontFamily}`;
    const paddingLeft = parseValue(textareaStyles.paddingLeft);
    const paddingRight = parseValue(textareaStyles.paddingRight);

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    context.font = font;

    const calculateNumLines = (str) => {
        const textareaWidth =
            textarea.getBoundingClientRect().width - paddingLeft - paddingRight;
        const words = str.split(' ');
        let lineCount = 0;
        let currentLine = '';
        
        for (let i = 0; i < words.length; i++) {
            const wordWidth = context.measureText(words[i] + ' ').width;
            const lineWidth = context.measureText(currentLine).width;

            if (lineWidth + wordWidth > textareaWidth) {
                lineCount++;
                currentLine = words[i] + ' ';
            } else {
                currentLine += words[i] + ' ';
            }
        }

        if (currentLine.trim() !== '') {
            lineCount++;
        }

        return lineCount;
    };

    const calculateLineNumbers = () => {
        const lines = textarea.value.split('\n');
        const numLines = lines.map((line) => calculateNumLines(line));

        let lineNumbers = [];
        let i = 1;
        while (numLines.length > 0) {
            const numLinesOfSentence = numLines.shift();
            lineNumbers.push(i);
            if (numLinesOfSentence > 1) {
                var x= Array(numLinesOfSentence - 1)
                    .fill('')
                    .forEach((_) => lineNumbers.push(''));
            }
            i++;
        }
//alert('numLines="' + numLines + '", lineNumbers="' + lineNumbers + '"');
        return lineNumbers;
    };

    const displayLineNumbers = () => {
        const lineNumbers = calculateLineNumbers();
        lineNumbersEle.innerHTML = Array.from({
            length: lineNumbers.length
        }, (_, i) => `<div>${lineNumbers[i] || '&nbsp;'}</div>`).join('');
    };

    textarea.addEventListener('input', () => {
        displayLineNumbers();
    });

    displayLineNumbers();

    const ro = new ResizeObserver(() => {
        const rect = textarea.getBoundingClientRect();
        lineNumbersEle.style.height = `${rect.height}px`/2;
/*        lineNumbersEle.style.height = `${rect.height}px`;*/
/*        lineNumbersEle.style.height = 280;*/
        displayLineNumbers();
    });
    ro.observe(textarea);

    textarea.addEventListener('scroll', () => {
        lineNumbersEle.scrollTop = textarea.scrollTop;
    });
});