/**
 * 数学公式渲染工具
 * 将LaTeX格式的数学公式或普通分数渲染成HTML
 */

/**
 * 渲染文本中的数学公式
 * 支持：
 * 1. 普通分数：2/5 → 漂亮的分数形式
 * 2. LaTeX公式：$\frac{2}{5}$ → 漂亮的分数形式
 * 3. 块级公式：$$公式$$
 * 
 * @param text 包含公式的文本
 * @returns 渲染后的HTML字符串
 */
export function renderMath(text: string): string {
  if (!text) return ''

  try {
    // 第1步：修正常见的LaTeX语法错误
    // \rac -> \frac (缺少f)
    text = text.replace(/\$\\rac\{/g, '$\\frac{')
    
    // 第2步：将普通分数转换为漂亮的HTML分数（不在$符号内的）
    // 匹配形如 2/5 的分数（数字/数字）
    text = text.replace(/(?<!\$)(\d+)\/(\d+)(?!\$)/g, (match, numerator, denominator) => {
      return `<span class="fraction"><sup>${numerator}</sup>⁄<sub>${denominator}</sub></span>`
    })

    // 第3步：渲染LaTeX公式（如果KaTeX可用）
    try {
      // 动态导入KaTeX（如果已安装）
      if (typeof window !== 'undefined') {
        // 尝试使用KaTeX渲染
        const hasKatex = checkKatexAvailable()
        if (hasKatex) {
          text = renderWithKatex(text)
        } else {
          // 如果KaTeX不可用，使用简单渲染
          text = renderSimpleLatex(text)
        }
      } else {
        text = renderSimpleLatex(text)
      }
    } catch (e) {
      // KaTeX不可用时，使用简单渲染
      text = renderSimpleLatex(text)
    }

    return text
  } catch (error) {
    console.error('数学公式渲染出错:', error)
    return text
  }
}

/**
 * 检查KaTeX是否可用
 */
function checkKatexAvailable(): boolean {
  try {
    // 尝试动态导入
    return false // 暂时禁用KaTeX，使用简单渲染
  } catch {
    return false
  }
}

/**
 * 简单的LaTeX渲染（不依赖KaTeX）
 */
function renderSimpleLatex(text: string): string {
  // 处理 $\frac{a}{b}$ 格式
  text = text.replace(/\$\\frac\{([^}]+)\}\{([^}]+)\}\$/g, (match, numerator, denominator) => {
    return `<span class="fraction"><sup>${numerator.trim()}</sup>⁄<sub>${denominator.trim()}</sub></span>`
  })
  
  // 处理 $x^2$ 格式（上标）
  text = text.replace(/\$([a-zA-Z0-9]+)\^([0-9]+)\$/g, (match, base, exponent) => {
    return `${base}<sup>${exponent}</sup>`
  })
  
  // 处理 $\sqrt{x}$ 格式（平方根）
  text = text.replace(/\$\\sqrt\{([^}]+)\}\$/g, (match, content) => {
    return `√${content}`
  })
  
  // 移除剩余的单独的$符号（如果有未处理的）
  // text = text.replace(/\$/g, '')
  
  return text
}

/**
 * 使用KaTeX渲染（需要安装katex包）
 */
function renderWithKatex(text: string): string {
  // 预留接口，如果后续需要使用KaTeX
  return renderSimpleLatex(text)
}

/**
 * 渲染单个数学公式
 * 
 * @param formula LaTeX公式
 * @param displayMode 是否为块级显示模式
 * @returns 渲染后的HTML字符串
 */
export function renderFormula(formula: string, displayMode: boolean = false): string {
  if (!formula) return ''

  try {
    return katex.renderToString(formula, {
      displayMode,
      throwOnError: false,
      strict: false
    })
  } catch (error) {
    console.error('公式渲染出错:', formula, error)
    return formula
  }
}

/**
 * 检查文本中是否包含数学公式
 */
export function hasMathFormula(text: string): boolean {
  if (!text) return false
  return /\$.*?\$|\\\w+\{.*?\}/.test(text)
}

